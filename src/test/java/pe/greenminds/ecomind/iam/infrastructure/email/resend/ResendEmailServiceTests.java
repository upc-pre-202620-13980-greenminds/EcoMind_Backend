package pe.greenminds.ecomind.iam.infrastructure.email.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.infrastructure.email.console.ConsoleEmailService;

class ResendEmailServiceTests {
  private MockRestServiceServer server;
  private ResendEmailService service;

  @BeforeEach
  void setUp() {
    var builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    var messages = new ResourceBundleMessageSource();
    messages.setBasename("messages");
    messages.setDefaultEncoding("UTF-8");
    messages.setFallbackToSystemLocale(false);
    service = new ResendEmailService(messages, "test-key", "EcoMind <sender@example.invalid>",
        "ecomind://authentication/reset-password?locale=en&token=previous", builder);
    LocaleContextHolder.setLocale(Locale.ENGLISH);
  }

  @AfterEach
  void resetLocale() {
    LocaleContextHolder.resetLocaleContext();
  }

  @Test
  void recoverySendsAnEncodedSingleUseLinkInHtmlAndPlainText() {
    String link = "ecomind://authentication/reset-password?locale=en&token=code%2B%26%3D";
    server.expect(requestTo("https://api.resend.com/emails"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Bearer test-key"))
        .andExpect(jsonPath("$.from").value("EcoMind <sender@example.invalid>"))
        .andExpect(jsonPath("$.to[0]").value("camila@example.invalid"))
        .andExpect(jsonPath("$.subject").value("Reset your EcoMind password"))
        .andExpect(jsonPath("$.html", containsString("locale=en&amp;token=code%2B%26%3D")))
        .andExpect(jsonPath("$.html", containsString("30 minutes")))
        .andExpect(jsonPath("$.text", containsString(link)))
        .andExpect(jsonPath("$.text", containsString("used once")))
        .andRespond(withSuccess("{\"id\":\"test-message\"}", MediaType.APPLICATION_JSON));

    service.sendPasswordRecoveryLink(new EmailAddress("camila@example.invalid"), "code+&=");

    server.verify();
  }

  @Test
  void recoveryUsesTheRequestLanguage() {
    LocaleContextHolder.setLocale(Locale.forLanguageTag("es-419"));
    server.expect(requestTo("https://api.resend.com/emails"))
        .andExpect(jsonPath("$.subject").value("Restablece tu contraseña de EcoMind"))
        .andExpect(jsonPath("$.text", containsString("30 minutos")))
        .andRespond(withSuccess("{\"id\":\"test-message\"}", MediaType.APPLICATION_JSON));

    service.sendPasswordRecoveryLink(new EmailAddress("camila@example.invalid"), "recovery-token");

    server.verify();
  }

  @Test
  void verificationEscapesTheNameAndProvidesPlainText() {
    server.expect(requestTo("https://api.resend.com/emails"))
        .andExpect(jsonPath("$.html", containsString("&lt;b&gt;Camila&lt;/b&gt;")))
        .andExpect(jsonPath("$.text", containsString("123456")))
        .andExpect(jsonPath("$.text", containsString("20 minutes")))
        .andRespond(withSuccess("{\"id\":\"test-message\"}", MediaType.APPLICATION_JSON));

    service.sendVerificationCode(new EmailAddress("camila@example.invalid"), "<b>Camila</b>", "123456");

    server.verify();
  }

  @Test
  void providerRejectionIsNotReportedAsSuccess() {
    server.expect(requestTo("https://api.resend.com/emails"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("camila@example.invalid"), "recovery-token"))
        .isInstanceOf(EmailDeliveryException.class)
        .hasCauseInstanceOf(RestClientResponseException.class);

    server.verify();
  }

  @Test
  void developmentUsesTheConsoleAdapter() {
    assertAdapter(ConsoleEmailService.class, "dev");
  }

  @Test
  void developmentCanExplicitlyEnableRealEmail() {
    assertAdapter(ResendEmailService.class, "dev", "email");
  }

  @Test
  void productionUsesResend() {
    assertAdapter(ResendEmailService.class, "prod");
  }

  @Test
  void testsNeverActivateTheRealProvider() {
    for (String profiles : new String[] {"test", "test,email", "test,prod", "test,dev", "test,dev,email"}) {
      try (var context = emailContext(profiles.split(","))) {
        assertThat(context.getBeansOfType(ResendEmailService.class)).isEmpty();
      }
    }
  }

  @Test
  void missingMailCredentialsFailStartup() {
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getEnvironment().setActiveProfiles("prod");
      context.register(PropertySourcesPlaceholderConfigurer.class, ResendEmailService.class);
      assertThatThrownBy(context::refresh).hasRootCauseInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  void theDefaultAndroidRouteIncludesTheIssuedToken() {
    var builder = RestClient.builder();
    var defaultServer = MockRestServiceServer.bindTo(builder).build();
    var messages = new ResourceBundleMessageSource();
    messages.setBasename("messages");
    messages.setFallbackToSystemLocale(false);
    var defaultService = new ResendEmailService(messages, "test-key", "sender@example.invalid",
        "ecomind://authentication/reset-password", builder);
    defaultServer.expect(requestTo("https://api.resend.com/emails"))
        .andExpect(jsonPath("$.text", containsString("ecomind://authentication/reset-password?token=issued-token")))
        .andRespond(withSuccess("{\"id\":\"test-message\"}", MediaType.APPLICATION_JSON));

    defaultService.sendPasswordRecoveryLink(new EmailAddress("camila@example.invalid"), "issued-token");

    defaultServer.verify();
  }

  private void assertAdapter(Class<? extends EmailService> expected, String... profiles) {
    try (var context = emailContext(profiles)) {
      assertThat(context.getBeansOfType(EmailService.class)).hasSize(1);
      assertThat(context.getBean(EmailService.class)).isInstanceOf(expected);
    }
  }

  private AnnotationConfigApplicationContext emailContext(String... profiles) {
    var context = new AnnotationConfigApplicationContext();
    context.getEnvironment().setActiveProfiles(profiles);
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
        "iam.email.resend.api-key=test-key", "iam.email.resend.from-email=sender@example.invalid",
        "iam.password-recovery.url=ecomind://authentication/reset-password");
    context.register(PropertySourcesPlaceholderConfigurer.class, ConsoleEmailService.class, ResendEmailService.class);
    context.refresh();
    return context;
  }
}
