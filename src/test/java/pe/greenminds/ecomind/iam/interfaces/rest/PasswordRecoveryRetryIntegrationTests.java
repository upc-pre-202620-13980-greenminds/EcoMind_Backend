package pe.greenminds.ecomind.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.domain.repositories.PasswordResetTokenRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordRecoveryRetryIntegrationTests {
  private static final String PATH = "/api/v1/authentication/password-recovery/confirm";
  private static final String CONFIRMATION_ID = "A".repeat(43);
  private static final String NEW_PASSWORD = "NewGreenPlanet2026";
  @Autowired MockMvc mvc;
  @Autowired AccountRepository accounts;
  @Autowired PasswordResetTokenRepository tokens;
  @Autowired PasswordHasher passwords;
  @Autowired JdbcTemplate jdbc;

  @Test
  void lostResponseCanBeAcknowledgedAfterCommitWithoutChangingThePasswordAgain() throws Exception {
    Fixture fixture = fixture(null, null);
    String body = body(fixture.token(), NEW_PASSWORD, CONFIRMATION_ID);
    confirm(body, 204);
    var storedHash = accounts.findById(fixture.accountId()).orElseThrow().getCredential().getPasswordHash();
    // A separate request and transaction sees the durable receipt of the original change.
    confirm(body, 204);
    assertThat(accounts.findById(fixture.accountId()).orElseThrow().getCredential().getPasswordHash())
        .isEqualTo(storedHash);
    String storedId = jdbc.queryForObject(
        "select confirmation_id_hash from password_reset_tokens where token_hash = ?",
        String.class, TokenHash.of(fixture.token()).value());
    assertThat(storedId).isEqualTo(TokenHash.of(CONFIRMATION_ID).value()).isNotEqualTo(CONFIRMATION_ID);
    confirm(body(fixture.token(), NEW_PASSWORD, null), 422);
    confirm(body(fixture.token(), NEW_PASSWORD, "B".repeat(43)), 422);
    confirm(body(fixture.token(), "DifferentPlanet2026", CONFIRMATION_ID), 422);
    confirm(body, 204);
  }

  @Test
  void legacyClientsStillHaveSingleUseLinks() throws Exception {
    Fixture fixture = fixture(null, null);
    String body = body(fixture.token(), NEW_PASSWORD, null);
    confirm(body, 204);
    confirm(body, 422);
    confirm(body(fixture.token(), NEW_PASSWORD, CONFIRMATION_ID), 422);
  }

  @Test
  void receiptExpiresAndCannotAcknowledgeAnotherLaterPassword() throws Exception {
    Fixture old = fixture(Instant.now().minusSeconds(301), TokenHash.of(CONFIRMATION_ID));
    confirm(body(old.token(), NEW_PASSWORD, CONFIRMATION_ID), 422);
    Fixture recent = fixture(Instant.now(), TokenHash.of(CONFIRMATION_ID));
    confirm(body(recent.token(), "DifferentPlanet2026", CONFIRMATION_ID), 422);
  }

  @Test
  void aReceiptAcknowledgesAPriorChangeEvenWhenTheOriginalLinkHasJustExpired() throws Exception {
    Fixture fixture = fixture(Instant.now().minusSeconds(2), TokenHash.of(CONFIRMATION_ID));
    jdbc.update("update password_reset_tokens set expires_at = ? where token_hash = ?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(1)), TokenHash.of(fixture.token()).value());
    confirm(body(fixture.token(), NEW_PASSWORD, CONFIRMATION_ID), 204);
    confirm(body(fixture.token(), "DifferentPlanet2026", CONFIRMATION_ID), 422);
    confirm(body(fixture.token(), NEW_PASSWORD, null), 422);
  }

  @Test
  void malformedConfirmationIdentifiersAreRejectedWithoutConsumingTheToken() throws Exception {
    Fixture fixture = fixture(null, null);
    confirm(body(fixture.token(), NEW_PASSWORD, "short"), 400);
    confirm(body(fixture.token(), NEW_PASSWORD, CONFIRMATION_ID), 204);
  }

  @Test
  void concurrentConfirmationsCanChangeThePasswordOnlyOnce() throws Exception {
    Fixture fixture = fixture(null, null);
    var barrier = new CyclicBarrier(2);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(() -> {
        barrier.await(10, TimeUnit.SECONDS);
        return perform(body(fixture.token(), NEW_PASSWORD, CONFIRMATION_ID));
      });
      var second = executor.submit(() -> {
        barrier.await(10, TimeUnit.SECONDS);
        return perform(body(fixture.token(), "DifferentPlanet2026", "B".repeat(43)));
      });
      assertThat(java.util.List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(204, 422);
    }
  }

  private int perform(String body) throws Exception {
    return mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();
  }

  private void confirm(String body, int expectedStatus) throws Exception {
    mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().is(expectedStatus));
  }

  private Fixture fixture(Instant usedAt, TokenHash confirmationHash) {
    Account account = Account.create(new EmailAddress(UUID.randomUUID() + "@example.com"),
        passwords.hash(usedAt == null ? "OldGreenPlanet2026" : NEW_PASSWORD));
    account.activate();
    account = accounts.save(account);
    String token = UUID.randomUUID().toString();
    tokens.save(new PasswordResetToken(null, account.getId(), TokenHash.of(token),
        Instant.now().plusSeconds(600), usedAt, confirmationHash));
    return new Fixture(account.getId(), token);
  }

  private static String body(String token, String password, String confirmationId) {
    return "{\"token\":\"" + token + "\",\"newPassword\":\"" + password + "\""
        + (confirmationId == null ? "" : ",\"confirmationId\":\"" + confirmationId + "\"") + "}";
  }

  private record Fixture(AccountId accountId, String token) {}
}
