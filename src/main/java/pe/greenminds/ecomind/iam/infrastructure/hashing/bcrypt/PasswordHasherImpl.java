package pe.greenminds.ecomind.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;

/**
 * Hashes passwords with BCrypt, which salts every hash and is slow on purpose.
 */
@Component
public class PasswordHasherImpl implements PasswordHasher {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public PasswordHash hash(String rawPassword) {
    return new PasswordHash(encoder.encode(rawPassword));
  }

  @Override
  public boolean matches(String rawPassword, PasswordHash passwordHash) {
    return encoder.matches(rawPassword, passwordHash.value());
  }
}
