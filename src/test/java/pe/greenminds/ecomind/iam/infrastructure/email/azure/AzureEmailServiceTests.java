package pe.greenminds.ecomind.iam.infrastructure.email.azure;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import java.time.Duration;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailDeliveryException;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.infrastructure.email.resend.ResendEmailService;

class AzureEmailServiceTests {
  private EmailClient client;
  private SyncPoller<EmailSendResult, EmailSendResult> poller;
  private AzureEmailService service;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    client = mock(EmailClient.class);
    poller = mock(SyncPoller.class);
    when(client.beginSend(any())).thenReturn(poller);
    var messages = new ResourceBundleMessageSource();
    messages.setBasename("messages");
    messages.setDefaultEncoding("UTF-8");
    messages.setFallbackToSystemLocale(false);
    service = new AzureEmailService(messages, "DoNotReply@example.invalid",
        "ecomind://authentication/reset-password?locale=en&token=old", client);
    LocaleContextHolder.setLocale(Locale.ENGLISH);
  }

  @AfterEach
  void resetLocale() { LocaleContextHolder.resetLocaleContext(); }

  @Test
  void verificationUsesEscapedHtmlAndPlainTextWithoutTracking() {
    completed(EmailSendStatus.SUCCEEDED);
    service.sendVerificationCode(new EmailAddress("demo@example.invalid"), "<b>Demo</b>", "123456");
    var message = sentMessage();
    assertThat(message.getSenderAddress()).isEqualTo("DoNotReply@example.invalid");
    assertThat(message.getToRecipients()).extracting("address").containsExactly("demo@example.invalid");
    assertThat(message.getSubject()).isEqualTo("Your EcoMind verification code");
    assertThat(message.getBodyHtml()).contains("&lt;b&gt;Demo&lt;/b&gt;", "123456");
    assertThat(message.getBodyPlainText()).contains("20 minutes", "123456");
    assertThat(message.isUserEngagementTrackingDisabled()).isTrue();
    assertThat(message.getBodyHtml()).contains("cid:ecomind-logo", "alt=\"EcoMind\"");
    assertThat(message.getAttachments()).hasSize(1);
    var logo = message.getAttachments().get(0);
    assertThat(logo.getContentId()).isEqualTo("ecomind-logo");
    assertThat(logo.getContentType()).isEqualTo("image/png");
    assertThat(logo.getContent().toBytes()).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47);
  }

  @Test
  void recoveryPreservesTheRequestLanguageAndReplacesTheTokenSafely() {
    completed(EmailSendStatus.SUCCEEDED);
    LocaleContextHolder.setLocale(Locale.forLanguageTag("es-419"));
    service.sendPasswordRecoveryLink(new EmailAddress("demo@example.invalid"), "code+&=");
    var message = sentMessage();
    assertThat(message.getSubject()).isEqualTo("Restablece tu contraseña de EcoMind");
    assertThat(message.getBodyHtml()).contains("locale=en&amp;token=code%2B%26%3D").doesNotContain("token=old");
    assertThat(message.getBodyPlainText()).contains("locale=en&token=code%2B%26%3D", "30 minutos");
  }

  @Test
  void failedCanceledAndUnconfirmedOperationsAreNotReportedAsSent() {
    for (var status : new EmailSendStatus[] {EmailSendStatus.FAILED, EmailSendStatus.CANCELED, EmailSendStatus.RUNNING}) {
      completed(status);
      assertThatThrownBy(() -> service.sendPasswordRecoveryLink(new EmailAddress("demo@example.invalid"), "token"))
          .isInstanceOf(EmailDeliveryException.class);
    }
    when(poller.waitForCompletion(Duration.ofSeconds(20))).thenReturn(
        new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(new EmailAddress("demo@example.invalid"), "token"))
        .isInstanceOf(EmailDeliveryException.class);
  }

  @Test
  void transportFailureBecomesTheIamDeliveryError() {
    when(client.beginSend(any())).thenThrow(new IllegalStateException("test transport failure"));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(new EmailAddress("demo@example.invalid"), "token"))
        .isInstanceOf(EmailDeliveryException.class).hasCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void configuredAzureProviderExcludesResendAndTestsExcludeBothProviders() {
    for (var profiles : new String[] {"prod", "test,prod", "test,dev,email"}) {
      try (var context = new AnnotationConfigApplicationContext()) {
        context.getEnvironment().setActiveProfiles(profiles.split(","));
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "iam.email.provider=azure",
            "iam.email.azure.connection-string=endpoint=https://example.communication.azure.com/;accesskey=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
            "iam.email.azure.from-email=DoNotReply@example.invalid",
            "iam.password-recovery.url=ecomind://authentication/reset-password");
        context.register(PropertySourcesPlaceholderConfigurer.class, AzureEmailService.class, ResendEmailService.class);
        context.refresh();
        assertThat(context.getBeansOfType(ResendEmailService.class)).isEmpty();
        assertThat(context.getBeansOfType(EmailService.class)).hasSize(profiles.startsWith("test") ? 0 : 1);
      }
    }
  }

  @Test
  void missingAzureSettingsFailBeforeSending() {
    assertThatThrownBy(() -> new AzureEmailService(null, "", "DoNotReply@example.invalid", "ecomind://authentication/reset-password"))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("Azure email connection string is required.");
    assertThatThrownBy(() -> new AzureEmailService(null, "", "ecomind://authentication/reset-password", client))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("Azure email sender is required.");
    verifyNoInteractions(client);
  }

  private void completed(EmailSendStatus status) {
    when(poller.waitForCompletion(Duration.ofSeconds(20))).thenReturn(new PollResponse<>(
        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, new EmailSendResult("test-operation", status, null)));
  }

  private EmailMessage sentMessage() {
    var capture = ArgumentCaptor.forClass(EmailMessage.class);
    verify(client).beginSend(capture.capture());
    return capture.getValue();
  }
}
