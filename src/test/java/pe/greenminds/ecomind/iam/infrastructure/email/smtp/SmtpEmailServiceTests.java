package pe.greenminds.ecomind.iam.infrastructure.email.smtp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.support.TestPropertySourceUtils;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.infrastructure.email.azure.AzureEmailService;
import pe.greenminds.ecomind.iam.infrastructure.email.console.ConsoleEmailService;
import pe.greenminds.ecomind.iam.infrastructure.email.resend.ResendEmailService;

class SmtpEmailServiceTests {
  private JavaMailSender sender;
  private MimeMessage message;
  private SmtpEmailService service;

  @BeforeEach
  void setUp() {
    sender = mock(JavaMailSender.class);
    message = new MimeMessage(Session.getInstance(new Properties()));
    when(sender.createMimeMessage()).thenReturn(message);
    service = new SmtpEmailService(messages(), sender, "sender@example.invalid",
        "https://example.invalid/authentication/reset-password");
    LocaleContextHolder.setLocale(Locale.ENGLISH);
  }

  @AfterEach
  void resetLocale() {
    LocaleContextHolder.resetLocaleContext();
  }

  @Test
  void verificationIncludesHtmlPlainTextAndTheInlineLogo() throws Exception {
    service.sendVerificationCode(new EmailAddress("camila@example.invalid"), "<b>Camila</b>", "123456");

    verify(sender).send(message);
    message.saveChanges();
    assertThat(message.getFrom()[0].toString()).isEqualTo("EcoMind <sender@example.invalid>");
    assertThat(message.getAllRecipients()[0].toString()).isEqualTo("camila@example.invalid");
    assertThat(message.getSubject()).isEqualTo("Your EcoMind verification code");
    Multipart mixed = (Multipart) message.getContent();
    Multipart related = (Multipart) mixed.getBodyPart(0).getContent();
    Multipart alternatives = (Multipart) related.getBodyPart(0).getContent();
    assertThat(alternatives.getBodyPart(0).getContent().toString()).contains("123456", "20 minutes");
    assertThat(alternatives.getBodyPart(1).getContent().toString())
        .contains("&lt;b&gt;Camila&lt;/b&gt;", "cid:ecomind-logo");
    assertThat(related.getBodyPart(1).getHeader("Content-ID")).containsExactly("<ecomind-logo>");
    assertThat(related.getBodyPart(1).getInputStream().readAllBytes()).isNotEmpty();
  }

  @Test
  void recoveryUsesTheRequestLanguageAndAnEncodedLink() throws Exception {
    LocaleContextHolder.setLocale(Locale.forLanguageTag("es-419"));

    service.sendPasswordRecoveryLink(new EmailAddress("camila@example.invalid"), "code+&=");

    assertThat(message.getSubject()).isEqualTo("Restablece tu contraseña de EcoMind");
    Multipart related = (Multipart) ((Multipart) message.getContent()).getBodyPart(0).getContent();
    Multipart alternatives = (Multipart) related.getBodyPart(0).getContent();
    assertThat(alternatives.getBodyPart(0).getContent().toString())
        .contains("https://example.invalid/authentication/reset-password?token=code%2B%26%3D", "30 minutos");
  }

  @Test
  void aRejectedSendIsNotReportedAsSuccess() {
    doThrow(new MailSendException("Provider rejected the message")).when(sender).send(any(MimeMessage.class));

    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("camila@example.invalid"), "token"))
        .isInstanceOf(EmailDeliveryException.class).hasCauseInstanceOf(MailSendException.class);
  }

  @Test
  void refusesToAuthenticateWhenTheServerDoesNotOfferTls() throws Exception {
    try (var socket = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
        var executor = Executors.newSingleThreadExecutor()) {
      socket.setSoTimeout(5000);
      var conversation = executor.submit(() -> {
        try (var client = socket.accept()) {
          client.setSoTimeout(15000);
          var output = client.getOutputStream();
          var input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.US_ASCII));
          output.write("220 localhost\r\n".getBytes(StandardCharsets.US_ASCII));
          output.flush();
          String hello = input.readLine();
          output.write("250 localhost\r\n".getBytes(StandardCharsets.US_ASCII));
          output.flush();
          String next = input.readLine();
          return hello + "\n" + next;
        }
      });
      var secureService = new SmtpEmailService(messages(), "localhost", socket.getLocalPort(),
          "sender@example.invalid", "not-a-real-secret", "sender@example.invalid", "ecomind://reset");

      assertThatThrownBy(() -> secureService.sendVerificationCode(
          new EmailAddress("camila@example.invalid"), "Camila", "123456"))
          .isInstanceOf(EmailDeliveryException.class);
      assertThat(conversation.get(20, TimeUnit.SECONDS)).startsWith("EHLO ").doesNotContain("AUTH", "MAIL FROM");
    }
  }

  @Test
  void productionSelectsOnlySmtpAndDevelopmentKeepsConsoleUnlessEmailIsEnabled() {
    assertAdapter(SmtpEmailService.class, "prod", "azure");
    assertAdapter(ConsoleEmailService.class, "dev");
    assertAdapter(SmtpEmailService.class, "dev", "email");
  }

  @Test
  void testsNeverActivateSmtp() {
    for (String profiles : new String[] {"test", "test,prod", "test,dev,email"}) {
      try (var context = emailContext(profiles.split(","))) {
        assertThat(context.getBeansOfType(SmtpEmailService.class)).isEmpty();
      }
    }
  }

  @Test
  void existingProvidersRemainSelectableWithSmtpOnTheClasspath() {
    for (String provider : new String[] {"resend", "azure"}) {
      try (var context = emailContext(provider, new String[] {"prod", "azure"})) {
        assertThat(context.getBeansOfType(EmailService.class)).hasSize(1);
        assertThat(context.getBean(EmailService.class))
            .isInstanceOf(provider.equals("azure") ? AzureEmailService.class : ResendEmailService.class);
      }
    }
  }

  @Test
  void missingCredentialsFailStartup() {
    assertThatThrownBy(() -> new SmtpEmailService(messages(), "smtp.gmail.com", 587,
        "sender@example.invalid", "", "sender@example.invalid", "ecomind://reset"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void assertAdapter(Class<? extends EmailService> expected, String... profiles) {
    try (var context = emailContext(profiles)) {
      assertThat(context.getBeansOfType(EmailService.class)).hasSize(1);
      assertThat(context.getBean(EmailService.class)).isInstanceOf(expected);
    }
  }

  private AnnotationConfigApplicationContext emailContext(String... profiles) {
    return emailContext("smtp", profiles);
  }

  private AnnotationConfigApplicationContext emailContext(String provider, String[] profiles) {
    var context = new AnnotationConfigApplicationContext();
    context.getEnvironment().setActiveProfiles(profiles);
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
        "iam.email.provider=" + provider, "iam.email.smtp.host=smtp.gmail.com", "iam.email.smtp.port=587",
        "iam.email.smtp.username=sender@example.invalid", "iam.email.smtp.password=test-only-password",
        "iam.email.smtp.from-email=sender@example.invalid", "iam.password-recovery.url=ecomind://reset",
        "iam.email.resend.api-key=test-only-key", "iam.email.resend.from-email=sender@example.invalid",
        "iam.email.azure.connection-string=endpoint=https://example.communication.azure.com/;accesskey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "iam.email.azure.from-email=sender@example.invalid");
    context.register(PropertySourcesPlaceholderConfigurer.class, ConsoleEmailService.class,
        ResendEmailService.class, AzureEmailService.class, SmtpEmailService.class);
    context.refresh();
    return context;
  }

  private ResourceBundleMessageSource messages() {
    var messages = new ResourceBundleMessageSource();
    messages.setBasename("messages");
    messages.setDefaultEncoding("UTF-8");
    messages.setFallbackToSystemLocale(false);
    return messages;
  }
}
