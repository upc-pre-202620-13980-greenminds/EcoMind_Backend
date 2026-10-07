package pe.greenminds.ecomind.iam.application.internal;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates the secrets sent by email: verification codes and recovery tokens.
 */
public final class SecureTokenGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int RECOVERY_TOKEN_BYTES = 32;

  private SecureTokenGenerator() {
  }

  /** Six-digit numeric code, zero-padded. */
  public static String newVerificationCode() {
    return "%06d".formatted(RANDOM.nextInt(1_000_000));
  }

  /** URL-safe random token, long enough to be unguessable. */
  public static String newRecoveryToken() {
    byte[] bytes = new byte[RECOVERY_TOKEN_BYTES];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
