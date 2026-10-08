package pe.greenminds.ecomind.iam.domain.services;

import org.springframework.stereotype.Service;

/**
 * Security rules a new password must meet, at registration and at password recovery:
 * 8 to 72 characters with at least one letter and one digit.
 */
@Service
public class PasswordPolicy {

  public static final int MIN_LENGTH = 8;
  // BCrypt ignores everything after 72 bytes.
  public static final int MAX_LENGTH = 72;

  public boolean isSatisfiedBy(String rawPassword) {
    if (rawPassword == null
        || rawPassword.length() < MIN_LENGTH
        || rawPassword.length() > MAX_LENGTH) {
      return false;
    }
    boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
    boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
    return hasLetter && hasDigit;
  }
}
