package pe.greenminds.ecomind.iam.infrastructure.email.resend;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pe.greenminds.ecomind.iam.infrastructure.email.IamEmailContentFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

/**
 * Sends the IAM emails through the Resend API, written in the language of the request.
 */
@Service
@Profile("!test & (!dev | email)")
@ConditionalOnProperty(name = "iam.email.provider", havingValue = "resend", matchIfMissing = true)
public class ResendEmailService implements EmailService {

  private static final String RESEND_API_URL = "https://api.resend.com";

  private final RestClient restClient;
  private final IamEmailContentFactory content;
  private final String fromEmail;

  @Autowired
  public ResendEmailService(
      MessageSource messageSource,
      @Value("${iam.email.resend.api-key}") String apiKey,
      @Value("${iam.email.resend.from-email}") String fromEmail,
      @Value("${iam.password-recovery.url}") String passwordRecoveryUrl) {
    this(messageSource, apiKey, fromEmail, passwordRecoveryUrl, emailClientBuilder());
  }

  ResendEmailService(
      MessageSource messageSource, String apiKey, String fromEmail, String passwordRecoveryUrl,
      RestClient.Builder builder) {
    org.springframework.util.Assert.hasText(apiKey, "Resend API key is required.");
    org.springframework.util.Assert.hasText(fromEmail, "Resend sender is required.");
    this.restClient =
        builder
            .baseUrl(RESEND_API_URL)
            .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
            .build();
    this.content = new IamEmailContentFactory(messageSource, passwordRecoveryUrl);
    this.fromEmail = fromEmail;
  }

  private static RestClient.Builder emailClientBuilder() {
    var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    var factory = new JdkClientHttpRequestFactory(client);
    factory.setReadTimeout(Duration.ofSeconds(10));
    return RestClient.builder().requestFactory(factory);
  }

  @Override
  public void sendVerificationCode(EmailAddress recipient, String name, String verificationCode) {
    send(recipient, content.verification(name, verificationCode));
  }

  @Override
  public void sendPasswordRecoveryLink(EmailAddress recipient, String recoveryToken) {
    send(recipient, content.recovery(recoveryToken));
  }

  private void send(EmailAddress recipient, IamEmailContentFactory.Content content) {
    try {
      restClient
        .post()
        .uri("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "from", fromEmail,
                "to", List.of(recipient.value()),
                "subject", content.subject(),
                "html", content.html(),
                "text", content.text()))
        .retrieve()
        .toBodilessEntity();
    } catch (RestClientException exception) {
      throw new EmailDeliveryException(exception);
    }
  }
}
