package pe.greenminds.ecomind.iam.infrastructure.email.azure;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import java.time.Duration;
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
  private static final Duration SEND_TIMEOUT = Duration.ofSeconds(20);
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
        .setUserEngagementTrackingDisabled(true);
    try {
      var result = client.beginSend(message).waitForCompletion(SEND_TIMEOUT).getValue();
      if (result == null || !EmailSendStatus.SUCCEEDED.equals(result.getStatus())) {
        throw new IllegalStateException("The email provider did not confirm acceptance.");
      }
    } catch (RuntimeException exception) {
      throw new EmailDeliveryException(exception);
    }
  }
}
