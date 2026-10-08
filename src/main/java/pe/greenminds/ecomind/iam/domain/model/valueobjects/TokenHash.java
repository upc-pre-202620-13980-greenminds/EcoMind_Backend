package pe.greenminds.ecomind.iam.domain.model.valueobjects;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 fingerprint of a sensitive token (verification code or recovery token), so the token
 * itself is never stored.
 */
public record TokenHash(String value) {

  public TokenHash {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Token hash is required");
    }
  }

  public static TokenHash of(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return new TokenHash(HexFormat.of().formatHex(hash));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }
}
