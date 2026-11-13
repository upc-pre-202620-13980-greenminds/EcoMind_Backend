package pe.greenminds.ecomind.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.application.outboundservices.UsersContextGateway;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.repositories.PendingRegistrationRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationRetryIntegrationTests {
  private static final String SIGN_UP_PATH = "/api/v1/authentication/sign-up";
  private static final String VERIFY_PATH = "/api/v1/authentication/verify-email";
  private static final String PASSWORD = "GreenPlanet2026";
  private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordHasher passwordHasher;
  @MockitoSpyBean PendingRegistrationRepository pendingRegistrations;
  @MockitoBean EmailService emailService;
  @MockitoBean UsersContextGateway usersContextGateway;

  @Test
  void concurrentFirstSignupsForOneEmailReplacePendingRegistrationInsteadOfFailingOnUniqueEmail()
      throws Exception {
    Assumptions.assumeTrue(isPostgres(), "The concurrent uniqueness race is PostgreSQL-specific");
    runConcurrentResubmissions("registration-race-" + UUID.randomUUID() + "@example.com", false);
  }

  @Test
  void concurrentResubmissionsReplaceAnExistingPendingRegistrationWithoutUniqueEmailFailure()
      throws Exception {
    Assumptions.assumeTrue(isPostgres(), "The concurrent uniqueness race is PostgreSQL-specific");
    runConcurrentResubmissions("registration-existing-" + UUID.randomUUID() + "@example.com", true);
  }

  private void runConcurrentResubmissions(String email, boolean seedExistingRegistration)
      throws Exception {
    if (seedExistingRegistration) {
      EmailAddress address = new EmailAddress(email);
      PendingRegistration prior = PendingRegistration.create(
          "Prior Registration", address, passwordHasher.hash(PASSWORD), SocialRole.PARENT,
          TokenHash.of("315792"), java.time.Instant.now());
      pendingRegistrations.save(prior);
    }

    CountDownLatch firstEmailCallBlocked = new CountDownLatch(1);
    CountDownLatch releaseFirstRequest = new CountDownLatch(1);
    CountDownLatch secondDeleteEntered = new CountDownLatch(1);
    AtomicInteger deleteCalls = new AtomicInteger();
    AtomicReference<Integer> secondBackendPid = new AtomicReference<>();
    List<String> sentCodes = new CopyOnWriteArrayList<>();

    doAnswer(invocation -> {
      EmailAddress requestedEmail = invocation.getArgument(0);
      if (email.equals(requestedEmail.value()) && deleteCalls.incrementAndGet() == 2) {
        // Signal before the real adapter call: the fixed adapter may now wait on its advisory lock.
        secondBackendPid.set(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
        secondDeleteEntered.countDown();
      }
      return invocation.callRealMethod();
    }).when(pendingRegistrations).deleteByEmail(any(EmailAddress.class));

    doAnswer(invocation -> {
      EmailAddress recipient = invocation.getArgument(0);
      String code = invocation.getArgument(2);
      if (email.equals(recipient.value())) {
        sentCodes.add(code);
        if (sentCodes.size() == 1) {
          firstEmailCallBlocked.countDown();
          if (!releaseFirstRequest.await(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting to release the first signup request");
          }
        }
      }
      return null;
    }).when(emailService).sendVerificationCode(any(EmailAddress.class), anyString(), anyString());

    try (var executor = Executors.newFixedThreadPool(2)) {
      try {
        var first = executor.submit(() -> submitRegistration(email));
        assertThat(firstEmailCallBlocked.await(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS)).isTrue();

        var second = executor.submit(() -> submitRegistration(email));
        assertThat(secondDeleteEntered.await(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS)).isTrue();
        assertThat(secondBackendPid.get()).isNotNull();
        assertThat(awaitPostgresLockWait(secondBackendPid.get(), LOCK_WAIT_TIMEOUT)).isTrue();

        releaseFirstRequest.countDown();
        List<Integer> statuses = List.of(
            first.get(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS),
            second.get(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS));
        assertThat(statuses).containsExactlyInAnyOrder(201, 201);
      } finally {
        releaseFirstRequest.countDown();
      }
    }

    assertThat(sentCodes).hasSize(2);
    assertThat(count("pending_registrations", email)).isEqualTo(1);
    assertThat(countVerificationRows(email)).isEqualTo(1);
    assertThat(storedVerificationHash(email)).isEqualTo(TokenHash.of(sentCodes.get(1)).value());

    if (!sentCodes.get(0).equals(sentCodes.get(1))) {
      assertThat(verifyEmail(email, sentCodes.get(0))).isEqualTo(422);
    }
    assertThat(verifyEmail(email, sentCodes.get(1))).isEqualTo(201);
    assertThat(count("pending_registrations", email)).isEqualTo(1);
    assertThat(countUsedVerifications(email)).isEqualTo(1);
  }

  @Test
  void sequentialResubmissionInvalidatesThePreviousCodeAndKeepsOnePendingRow() throws Exception {
    String email = "registration-resend-" + UUID.randomUUID() + "@example.com";
    List<String> sentCodes = new CopyOnWriteArrayList<>();
    doAnswer(invocation -> {
      EmailAddress recipient = invocation.getArgument(0);
      if (email.equals(recipient.value())) {
        sentCodes.add(invocation.getArgument(2));
      }
      return null;
    }).when(emailService).sendVerificationCode(any(EmailAddress.class), anyString(), anyString());

    assertThat(submitRegistration(email)).isEqualTo(201);
    assertThat(submitRegistration(email)).isEqualTo(201);
    assertThat(sentCodes).hasSize(2);
    assertThat(count("pending_registrations", email)).isEqualTo(1);
    assertThat(countVerificationRows(email)).isEqualTo(1);
    assertThat(storedVerificationHash(email)).isEqualTo(TokenHash.of(sentCodes.get(1)).value());
    if (!sentCodes.get(0).equals(sentCodes.get(1))) {
      assertThat(verifyEmail(email, sentCodes.get(0))).isEqualTo(422);
    }
    assertThat(verifyEmail(email, sentCodes.get(1))).isEqualTo(201);
  }

  private int submitRegistration(String email) throws Exception {
    String body = "{\"name\":\"Registration Retry\",\"email\":\"" + email
        + "\",\"password\":\"" + PASSWORD + "\",\"socialRole\":\"PARENT\"}";
    return mvc.perform(post(SIGN_UP_PATH).contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();
  }

  private int verifyEmail(String email, String code) throws Exception {
    String body = "{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}";
    return mvc.perform(post(VERIFY_PATH).contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();
  }

  private boolean isPostgres() {
    try (var connection = jdbc.getDataSource().getConnection()) {
      return connection.getMetaData().getDatabaseProductName().equals("PostgreSQL");
    } catch (java.sql.SQLException exception) {
      throw new IllegalStateException("Could not identify the integration database", exception);
    }
  }

  private boolean awaitPostgresLockWait(int pid, Duration timeout) throws InterruptedException {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadline) {
      String waitType = jdbc.queryForObject(
          "select wait_event_type from pg_stat_activity where pid = ?", String.class, pid);
      if ("Lock".equals(waitType)) {
        return true;
      }
      Thread.sleep(25);
    }
    return false;
  }

  private int count(String table, String email) {
    return jdbc.queryForObject("select count(*) from " + table + " where email = ?", Integer.class, email);
  }

  private int countVerificationRows(String email) {
    return jdbc.queryForObject("""
        select count(*)
        from email_verifications ev
        join pending_registrations pr on pr.id = ev.pending_registration_id
        where pr.email = ?
        """, Integer.class, email);
  }

  private int countUsedVerifications(String email) {
    return jdbc.queryForObject("""
        select count(*)
        from email_verifications ev
        join pending_registrations pr on pr.id = ev.pending_registration_id
        where pr.email = ? and ev.used_at is not null
        """, Integer.class, email);
  }

  private String storedVerificationHash(String email) {
    return jdbc.queryForObject("""
        select ev.token_hash
        from email_verifications ev
        join pending_registrations pr on pr.id = ev.pending_registration_id
        where pr.email = ?
        """, String.class, email);
  }
}
