package pe.greenminds.ecomind.iam.interfaces.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/** HTTPS entry for mail clients that remove custom-scheme links. GET never consumes the token. */
@io.swagger.v3.oas.annotations.Hidden
@RestController
public class PasswordRecoveryPageController {
  private final MessageSource messages;
  private final String template;

  public PasswordRecoveryPageController(MessageSource messages) throws IOException {
    this.messages = messages;
    template = new ClassPathResource("templates/password-recovery.html")
        .getContentAsString(StandardCharsets.UTF_8);
  }

  @GetMapping("/authentication/reset-password")
  public ResponseEntity<String> openApplication(
      @RequestParam(name = "token", required = false) String token, Locale locale) {
    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noStore());
    headers.set("Referrer-Policy", "no-referrer");
    headers.set("X-Robots-Tag", "noindex, nofollow");
    headers.set("Content-Security-Policy",
        "default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; frame-ancestors 'none'; form-action 'none'");
    if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
      return ResponseEntity.badRequest().headers(headers).contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
          .body(messages.getMessage("iam.recovery.page.invalid", null, locale));
    }
    String html = template.replace("{{language}}", message("lang", locale))
        .replace("{{title}}", message("title", locale))
        .replace("{{body}}", message("body", locale))
        .replace("{{open}}", message("open", locale))
        .replace("{{notice}}", message("notice", locale))
        .replace("{{link}}", HtmlUtils.htmlEscape("ecomind://authentication/reset-password?token=" + token, "UTF-8"));
    return ResponseEntity.ok().headers(headers)
        .contentType(new MediaType("text", "html", StandardCharsets.UTF_8)).body(html);
  }

  private String message(String key, Locale locale) {
    return HtmlUtils.htmlEscape(messages.getMessage("iam.recovery.page." + key, null, locale), "UTF-8");
  }
}
