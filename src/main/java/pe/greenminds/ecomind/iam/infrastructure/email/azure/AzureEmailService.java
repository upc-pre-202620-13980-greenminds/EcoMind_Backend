package pe.greenminds.ecomind.iam.infrastructure.email.azure;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailAttachment;
import com.azure.core.util.BinaryData;
import pe.greenminds.ecomind.iam.infrastructure.email.EmailBranding;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.infrastructure.email.IamEmailContentFactory;

@Service
@Profile("!test & (!dev | email)")
@ConditionalOnProperty(name = "iam.email.provider", havingValue = "azure")
public class AzureEmailService implements EmailService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AzureEmailService.class);
  private static final Duration SEND_TIMEOUT = Duration.ofSeconds(60);
  private final EmailClient client;
  private final String sender;
  private final IamEmailContentFactory content;

  @Autowired
  public AzureEmailService(MessageSource messages,
      @Value("${iam.email.azure.connection-string}") String connectionString,
      @Value("${iam.email.azure.from-email}") String sender,
      @Value("${iam.password-recovery.url}") String recoveryUrl) {
    this(messages, sender, recoveryUrl, new EmailClientBuilder()
        .connectionString(requireConnectionString(connectionString))
        .httpClient(HttpClient.createDefault(new HttpClientOptions()
            .setConnectTimeout(Duration.ofSeconds(5)).setReadTimeout(Duration.ofSeconds(10))
            .setResponseTimeout(Duration.ofSeconds(10))))
        .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofSeconds(1))))
        .buildClient());
  }

  AzureEmailService(MessageSource messages, String sender, String recoveryUrl, EmailClient client) {
    Assert.hasText(sender, "Azure email sender is required.");
    org.springframework.util.Assert.isTrue(EmailBranding.logoBytes().length > 0, "EcoMind email logo is empty.");
    this.client = client;
    this.sender = sender;
    this.content = new IamEmailContentFactory(messages, recoveryUrl);
  }

  private static String requireConnectionString(String value) {
    Assert.hasText(value, "Azure email connection string is required.");
    return value;
  }

  @Override
  public void sendVerificationCode(EmailAddress recipient, String name, String code) {
    send(recipient, content.verification(name, code));
  }

  @Override
  public void sendPasswordRecoveryLink(EmailAddress recipient, String token) {
    send(recipient, content.recovery(token));
  }

  private void send(EmailAddress recipient, IamEmailContentFactory.Content content) {
    var message = new EmailMessage().setSenderAddress(sender).setToRecipients(recipient.value())
        .setSubject(content.subject()).setBodyHtml(content.html()).setBodyPlainText(content.text())
        .setUserEngagementTrackingDisabled(true)
        .setAttachments(new EmailAttachment(EmailBranding.LOGO_FILENAME, "image/png",
            BinaryData.fromBytes(EmailBranding.logoBytes())).setContentId(EmailBranding.LOGO_CONTENT_ID));
    String operationId = "unavailable";
    try {
      var poller = client.beginSend(message);
      var initial = poller.poll();
      if (initial != null && initial.getValue() != null) {
        operationId = safeOperationId(initial.getValue().getId());
      }
      var completion = poller.waitForCompletion(SEND_TIMEOUT);
      var result = completion == null ? null : completion.getValue();
      if (result == null || !EmailSendStatus.SUCCEEDED.equals(result.getStatus())) {
        String status = result == null ? "missing"
            : EmailSendStatus.FAILED.equals(result.getStatus()) ? "failed"
            : EmailSendStatus.CANCELED.equals(result.getStatus()) ? "canceled" : "unconfirmed";
        String errorCode = result == null || result.getError() == null ? "none"
            : safeErrorCode(result.getError().getCode(), recipient, content);
        if (result != null) {
          operationId = safeOperationId(result.getId());
        }
        LOGGER.warn("email_acceptance_unconfirmed provider=azure result_status={} error_code={} operation_id={}",
            status, errorCode, operationId);
        throw new EmailDeliveryException(
            new IllegalStateException("The email provider did not confirm acceptance."));
      }
      LOGGER.info("email_accepted provider=azure operation_id={}", safeOperationId(result.getId()));
    } catch (EmailDeliveryException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      // SDK messages and response bodies can include recipients or credentials.
      LOGGER.warn("email_acceptance_unconfirmed provider=azure failure_type={} cause_type={} http_status={} operation_id={}",
          exception.getClass().getSimpleName(), causeType(exception), httpStatus(exception), operationId);
      throw new EmailDeliveryException(exception);
    }
  }

  private static String safeErrorCode(String code, EmailAddress recipient,
      IamEmailContentFactory.Content content) {
    return code != null && code.matches("[A-Za-z][A-Za-z0-9]{1,63}")
        && !recipient.value().contains(code) && !content.text().contains(code) ? code : "redacted";
  }

  private static String safeOperationId(String id) {
    return id != null && id.matches("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")
        ? id : "unavailable";
  }

  private static String causeType(Throwable exception) {
    for (int depth = 0; depth < 7 && exception.getCause() != null; depth++) {
      exception = exception.getCause();
    }
    return exception.getClass().getSimpleName();
  }

  private static int httpStatus(Throwable exception) {
    for (int depth = 0; exception != null && depth < 8; depth++, exception = exception.getCause()) {
      if (exception instanceof HttpResponseException response && response.getResponse() != null) {
        return response.getResponse().getStatusCode();
      }
    }
    return 0;
  }
}
