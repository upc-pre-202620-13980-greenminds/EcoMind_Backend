package pe.greenminds.ecomind.iam.infrastructure.email;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;

/** Builds the same localized messages for each delivery provider. */
public final class IamEmailContentFactory {
  private final MessageSource messages;
  private final String recoveryUrl;

  public IamEmailContentFactory(MessageSource messages, String recoveryUrl) {
    this.messages = messages;
    this.recoveryUrl = recoveryUrl;
  }

  public Content verification(String name, String code) {
    Locale locale = LocaleContextHolder.getLocale();
    return new Content(
        messages.getMessage("iam.email.verification.subject", null, locale),
        messages.getMessage("iam.email.verification.body",
            new Object[] {HtmlUtils.htmlEscape(name), code, PendingRegistration.VALIDITY.toMinutes()}, locale),
        messages.getMessage("iam.email.verification.text",
            new Object[] {name, code, PendingRegistration.VALIDITY.toMinutes()}, locale));
  }

  public Content recovery(String token) {
    Locale locale = LocaleContextHolder.getLocale();
    String link = UriComponentsBuilder.fromUriString(recoveryUrl)
        .replaceQueryParam("token", "{token}").encode().buildAndExpand(token).toUriString();
    return new Content(
        messages.getMessage("iam.email.recovery.subject", null, locale),
        messages.getMessage("iam.email.recovery.body",
            new Object[] {HtmlUtils.htmlEscape(link), PasswordResetToken.VALIDITY.toMinutes()}, locale),
        messages.getMessage("iam.email.recovery.text",
            new Object[] {link, PasswordResetToken.VALIDITY.toMinutes()}, locale));
  }

  public record Content(String subject, String html, String text) {}
}
