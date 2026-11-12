package pe.greenminds.ecomind.iam.infrastructure.email.azure;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.models.ResponseError;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import java.time.Duration;
import java.util.Locale;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
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
  private ch.qos.logback.classic.Logger serviceLogger;
  private ListAppender<ILoggingEvent> logAppender;

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
  void resetLocale() {
    LocaleContextHolder.resetLocaleContext();
    if (serviceLogger != null && logAppender != null) {
      serviceLogger.detachAppender(logAppender);
      logAppender.stop();
    }
  }

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
  void acceptedOperationsCanBeCorrelatedWithoutClaimingDeliveryOrLoggingPrivateValues() {
    var logs = captureLogs();
    var operationId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    when(poller.waitForCompletion(Duration.ofSeconds(60))).thenReturn(
        new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new EmailSendResult(operationId, EmailSendStatus.SUCCEEDED, null)));
    service.sendPasswordRecoveryLink(new EmailAddress("private-recipient@example.invalid"), "private-token");

    assertThat(formattedLogs(logs)).containsExactly(
        "email_accepted provider=azure operation_id=" + operationId);

    when(poller.waitForCompletion(Duration.ofSeconds(60))).thenReturn(
        new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new EmailSendResult("private-token", EmailSendStatus.SUCCEEDED, null)));
    service.sendPasswordRecoveryLink(new EmailAddress("private-recipient@example.invalid"), "private-token");

    assertThat(formattedLogs(logs)).containsExactly(
        "email_accepted provider=azure operation_id=" + operationId,
        "email_accepted provider=azure operation_id=unavailable");
    verify(poller, times(2)).setPollInterval(Duration.ofSeconds(30));
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
    when(poller.waitForCompletion(Duration.ofSeconds(60))).thenReturn(
        new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(new EmailAddress("demo@example.invalid"), "token"))
        .isInstanceOf(EmailDeliveryException.class);
  }

  @Test
  void timeoutPreservesInitiallyPolledOperationIdWithoutRetryingSend() {
    var logs = captureLogs();
    var operationId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    when(poller.poll()).thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
        new EmailSendResult(operationId, EmailSendStatus.RUNNING, null)));
    when(poller.waitForCompletion(Duration.ofSeconds(60))).thenThrow(
        new IllegalStateException("private recipient and token must not appear",
            new java.util.concurrent.TimeoutException("private provider detail")));

    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("private-recipient@example.invalid"), "private-recovery-token"))
        .isInstanceOf(EmailDeliveryException.class);

    verify(client, times(1)).beginSend(any());
    assertThat(formattedLogs(logs)).containsExactly(
        "email_acceptance_unconfirmed provider=azure failure_type=IllegalStateException "
            + "cause_type=TimeoutException http_status=0 operation_id=" + operationId);
    assertThat(formattedLogs(logs).get(0))
        .doesNotContain("private-recipient@example.invalid", "private-recovery-token", "private provider detail");
  }

  @Test
  void transportFailureBecomesTheIamDeliveryError() {
    when(client.beginSend(any())).thenThrow(new IllegalStateException("test transport failure"));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(new EmailAddress("demo@example.invalid"), "token"))
        .isInstanceOf(EmailDeliveryException.class).hasCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void failedAndNullProviderResultsHaveSafeDistinctClassifications() {
    var logs = captureLogs();
    completed(EmailSendStatus.FAILED);
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    when(poller.waitForCompletion(Duration.ofSeconds(60))).thenReturn(
        new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    assertThat(formattedLogs(logs)).contains(
        "email_acceptance_unconfirmed provider=azure result_status=failed error_code=none operation_id=unavailable",
        "email_acceptance_unconfirmed provider=azure result_status=missing error_code=none operation_id=unavailable");
  }

  @Test
  void httpResponseStatusIsReportedForDirectNestedAndMissingResponses() {
    var logs = captureLogs();
    var directResponse = mock(HttpResponse.class);
    when(directResponse.getStatusCode()).thenReturn(503);
    var direct = new HttpResponseException("synthetic direct provider detail", directResponse);
    when(client.beginSend(any())).thenThrow(direct);
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    reset(client);
    var nestedResponse = mock(HttpResponse.class);
    when(nestedResponse.getStatusCode()).thenReturn(429);
    var nested = new HttpResponseException("synthetic nested provider detail", nestedResponse);
    var wrapped = new IllegalStateException("synthetic wrapper detail",
        new RuntimeException("synthetic intermediate detail", nested));
    when(client.beginSend(any())).thenThrow(wrapped);
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    reset(client);
    when(client.beginSend(any())).thenThrow(
        new HttpResponseException("synthetic response-less detail", (HttpResponse) null));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    assertThat(formattedLogs(logs)).contains(
        "email_acceptance_unconfirmed provider=azure failure_type=HttpResponseException cause_type=HttpResponseException http_status=503 operation_id=unavailable",
        "email_acceptance_unconfirmed provider=azure failure_type=IllegalStateException cause_type=HttpResponseException http_status=429 operation_id=unavailable",
        "email_acceptance_unconfirmed provider=azure failure_type=HttpResponseException cause_type=HttpResponseException http_status=0 operation_id=unavailable");
  }

  @Test
  void providerExceptionMessagesNeverLeakRecipientTokenOrCredentialIntoLogs() {
    var logs = captureLogs();
    var recipient = "private-recipient@example.invalid";
    var token = "synthetic-recovery-token-secret";
    var credential = "synthetic-communication-key-secret";
    when(client.beginSend(any())).thenThrow(new IllegalStateException(
        "provider detail recipient=" + recipient + " token=" + token + " credential=" + credential));

    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress(recipient), token))
        .isInstanceOf(EmailDeliveryException.class);

    assertThat(logs.list).hasSize(1);
    assertThat(logs.list.get(0).getFormattedMessage())
        .contains("failure_type=IllegalStateException", "cause_type=IllegalStateException", "http_status=0")
        .doesNotContain(recipient, token, credential, "provider detail");
  }

  @Test
  void failedResultIncludesAWellFormedProviderCodeAndCanonicalOperationId() {
    var logs = captureLogs();
    var operationId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    completed(new EmailSendResult(operationId, EmailSendStatus.FAILED,
        new ResponseError("InvalidRequest", "synthetic provider detail")));

    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    assertThat(formattedLogs(logs)).containsExactly(
        "email_acceptance_unconfirmed provider=azure result_status=failed error_code=InvalidRequest operation_id="
            + operationId);
  }

  @Test
  void missingProviderErrorAndInvalidOperationIdUseSafePlaceholders() {
    var logs = captureLogs();
    completed(new EmailSendResult("not-a-canonical-id", EmailSendStatus.FAILED, null));
    assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
        new EmailAddress("demo@example.invalid"), "synthetic-token"))
        .isInstanceOf(EmailDeliveryException.class);

    assertThat(formattedLogs(logs)).containsExactly(
        "email_acceptance_unconfirmed provider=azure result_status=failed error_code=none operation_id=unavailable");
  }

  @Test
  void recipientAndBodyReflectedOrUnsafeProviderCodesAreRedacted() {
    var logs = captureLogs();
    var recipient = "private-recipient@example.invalid";
    var token = "SyntheticRecoveryToken123";
    var reflectedAndUnsafeCodes = new String[] {
        "private", // valid code syntax, but present in the recipient address
        token, // valid code syntax, but present in the recovery message text
        "Bad.Request", // punctuation is outside the safe provider-code format
        "A" + "b".repeat(64) // over the 64-character limit
    };

    for (var code : reflectedAndUnsafeCodes) {
      completed(new EmailSendResult("f47ac10b-58cc-4372-a567-0e02b2c3d479",
          EmailSendStatus.FAILED, new ResponseError(code, "synthetic provider error message")));
      assertThatThrownBy(() -> service.sendPasswordRecoveryLink(
          new EmailAddress(recipient), token))
          .isInstanceOf(EmailDeliveryException.class);
    }

    assertThat(formattedLogs(logs)).hasSize(reflectedAndUnsafeCodes.length)
        .allSatisfy(line -> assertThat(line)
            .contains("result_status=failed error_code=redacted operation_id=f47ac10b-58cc-4372-a567-0e02b2c3d479")
            .doesNotContain(recipient, token, "synthetic provider error message", "Bad.Request"));
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
    completed(new EmailSendResult("test-operation", status, null));
  }

  private void completed(EmailSendResult result) {
    when(poller.waitForCompletion(Duration.ofSeconds(60))).thenReturn(new PollResponse<>(
        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result));
  }

  private EmailMessage sentMessage() {
    var capture = ArgumentCaptor.forClass(EmailMessage.class);
    verify(client).beginSend(capture.capture());
    return capture.getValue();
  }

  private ListAppender<ILoggingEvent> captureLogs() {
    serviceLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AzureEmailService.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    serviceLogger.addAppender(logAppender);
    return logAppender;
  }

  private static java.util.List<String> formattedLogs(ListAppender<ILoggingEvent> appender) {
    return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
  }
}
