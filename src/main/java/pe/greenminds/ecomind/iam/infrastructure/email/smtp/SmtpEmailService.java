package pe.greenminds.ecomind.iam.infrastructure.email.smtp;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.infrastructure.email.EmailBranding;
import pe.greenminds.ecomind.iam.infrastructure.email.IamEmailContentFactory;

@Service
@Profile("!test & (!dev | email)")
@ConditionalOnProperty(name = "iam.email.provider", havingValue = "smtp")
public class SmtpEmailService implements EmailService {
  private final JavaMailSender sender;
  private final IamEmailContentFactory content;
  private final String fromEmail;

  @Autowired
  public SmtpEmailService(
      MessageSource messageSource,
      @Value("${iam.email.smtp.host}") String host,
      @Value("${iam.email.smtp.port:587}") int port,
      @Value("${iam.email.smtp.username}") String username,
      @Value("${iam.email.smtp.password}") String password,
      @Value("${iam.email.smtp.from-email}") String fromEmail,
      @Value("${iam.password-recovery.url}") String passwordRecoveryUrl) {
    this(messageSource, mailSender(host, port, username, password), fromEmail, passwordRecoveryUrl);
  }

  SmtpEmailService(
      MessageSource messageSource, JavaMailSender sender, String fromEmail, String passwordRecoveryUrl) {
    Assert.hasText(fromEmail, "SMTP sender is required.");
    this.sender = sender;
    this.content = new IamEmailContentFactory(messageSource, passwordRecoveryUrl);
    this.fromEmail = fromEmail;
  }

  private static JavaMailSender mailSender(String host, int port, String username, String password) {
    Assert.hasText(host, "SMTP host is required.");
    Assert.hasText(username, "SMTP username is required.");
    Assert.hasText(password, "SMTP password is required.");
    var sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(port);
    sender.setUsername(username);
    sender.setPassword(password);
    sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
    var properties = sender.getJavaMailProperties();
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.smtp.starttls.required", "true");
    properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
    properties.setProperty("mail.smtp.connectiontimeout", "5000");
    properties.setProperty("mail.smtp.timeout", "10000");
    properties.setProperty("mail.smtp.writetimeout", "10000");
    return sender;
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
      var message = sender.createMimeMessage();
      var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
      helper.setFrom(fromEmail, "EcoMind");
      helper.setTo(recipient.value());
      helper.setSubject(content.subject());
      helper.setText(content.text(), content.html());
      helper.addInline(EmailBranding.LOGO_CONTENT_ID,
          new ByteArrayResource(EmailBranding.logoBytes()), "image/png");
      sender.send(message);
    } catch (MessagingException | MailException | UnsupportedEncodingException exception) {
      throw new EmailDeliveryException(exception);
    }
  }
}
