package pe.greenminds.ecomind.iam.infrastructure.email.resend;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

/**
 * Sends the IAM emails through the Resend API, written in the language of the request.
 */
@Service
@Profile("!test & (!dev | email)")
public class ResendEmailService implements EmailService {

  private static final String RESEND_API_URL = "https://api.resend.com";

  private final RestClient restClient;
  private final MessageSource messageSource;
  private final String fromEmail;
  private final String passwordRecoveryUrl;

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
    this.restClient =
        builder
            .baseUrl(RESEND_API_URL)
            .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
            .build();
    this.messageSource = messageSource;
    this.fromEmail = fromEmail;
    this.passwordRecoveryUrl = passwordRecoveryUrl;
  }

  private static RestClient.Builder emailClientBuilder() {
    var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    var factory = new JdkClientHttpRequestFactory(client);
    factory.setReadTimeout(Duration.ofSeconds(10));
    return RestClient.builder().requestFactory(factory);
  }

  @Override
  public void sendVerificationCode(EmailAddress recipient, String name, String verificationCode) {
    Locale locale = LocaleContextHolder.getLocale();
    String subject = messageSource.getMessage("iam.email.verification.subject", null, locale);
    String body =
        messageSource.getMessage(
            "iam.email.verification.body",
            new Object[] {
                HtmlUtils.htmlEscape(name),
                verificationCode,
                PendingRegistration.VALIDITY.toMinutes()
            },
            locale);
    String text = messageSource.getMessage(
        "iam.email.verification.text",
        new Object[] {name, verificationCode, PendingRegistration.VALIDITY.toMinutes()}, locale);
    send(recipient, subject, body, text);
  }

  @Override
  public void sendPasswordRecoveryLink(EmailAddress recipient, String recoveryToken) {
    Locale locale = LocaleContextHolder.getLocale();
    String link =
        UriComponentsBuilder.fromUriString(passwordRecoveryUrl)
            .replaceQueryParam("token", "{token}")
            .encode()
            .buildAndExpand(recoveryToken)
            .toUriString();
    String subject = messageSource.getMessage("iam.email.recovery.subject", null, locale);
    String body =
        messageSource.getMessage(
            "iam.email.recovery.body",
            new Object[] {HtmlUtils.htmlEscape(link), PasswordResetToken.VALIDITY.toMinutes()},
            locale);
    String text = messageSource.getMessage(
        "iam.email.recovery.text",
        new Object[] {link, PasswordResetToken.VALIDITY.toMinutes()}, locale);
    send(recipient, subject, body, text);
  }

  private void send(EmailAddress recipient, String subject, String htmlBody, String textBody) {
    try {
      restClient
        .post()
        .uri("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "from", fromEmail,
                "to", List.of(recipient.value()),
                "subject", subject,
                "html", htmlBody,
                "text", textBody))
        .retrieve()
        .toBodilessEntity();
    } catch (RestClientException exception) {
      throw new EmailDeliveryException(exception);
    }
  }
}
