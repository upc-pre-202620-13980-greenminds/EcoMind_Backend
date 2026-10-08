package pe.greenminds.ecomind.iam.infrastructure.email.resend;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

/**
 * Sends the IAM emails through the Resend API, written in the language of the request.
 */
@Service
@Profile("!dev & !test")
public class ResendEmailService implements EmailService {

  private static final String RESEND_API_URL = "https://api.resend.com";

  private final RestClient restClient;
  private final MessageSource messageSource;
  private final String fromEmail;
  private final String passwordRecoveryUrl;

  public ResendEmailService(
      MessageSource messageSource,
      @Value("${iam.email.resend.api-key}") String apiKey,
      @Value("${iam.email.resend.from-email}") String fromEmail,
      @Value("${iam.password-recovery.url}") String passwordRecoveryUrl) {
    this.restClient =
        RestClient.builder()
            .baseUrl(RESEND_API_URL)
            .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
            .build();
    this.messageSource = messageSource;
    this.fromEmail = fromEmail;
    this.passwordRecoveryUrl = passwordRecoveryUrl;
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
    send(recipient, subject, body);
  }

  @Override
  public void sendPasswordRecoveryLink(EmailAddress recipient, String recoveryToken) {
    Locale locale = LocaleContextHolder.getLocale();
    String link =
        UriComponentsBuilder.fromUriString(passwordRecoveryUrl)
            .queryParam("token", recoveryToken)
            .build()
            .toUriString();
    String subject = messageSource.getMessage("iam.email.recovery.subject", null, locale);
    String body =
        messageSource.getMessage(
            "iam.email.recovery.body",
            new Object[] {link, PasswordResetToken.VALIDITY.toMinutes()},
            locale);
    send(recipient, subject, body);
  }

  private void send(EmailAddress recipient, String subject, String htmlBody) {
    restClient
        .post()
        .uri("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "from", fromEmail,
                "to", List.of(recipient.value()),
                "subject", subject,
                "html", htmlBody))
        .retrieve()
        .toBodilessEntity();
  }
}
