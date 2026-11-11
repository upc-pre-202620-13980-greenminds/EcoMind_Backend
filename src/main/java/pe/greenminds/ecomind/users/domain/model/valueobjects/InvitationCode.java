package pe.greenminds.ecomind.users.domain.model.valueobjects;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Code a user shares so that a friend can send them a friend request without knowing their id.
 * It has 8 characters: capital letters and digits, without 0, O, 1 and I, which are easy to mix up
 * when the code is read aloud or typed.
 */
public record InvitationCode(String value) {

  public static final int LENGTH = 8;
  public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  public static final String FORMAT = "^[A-HJ-NP-Z2-9]{8}$";

  private static final Pattern PATTERN = Pattern.compile(FORMAT);
  private static final SecureRandom RANDOM = new SecureRandom();

  public InvitationCode {
    if (value == null || !PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Invitation code must have 8 capital letters or digits, without 0, O, 1 and I");
    }
  }

  /** A random code; it is not checked against the codes that already exist. */
  public static InvitationCode generate() {
    StringBuilder code = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return new InvitationCode(code.toString());
  }
}
