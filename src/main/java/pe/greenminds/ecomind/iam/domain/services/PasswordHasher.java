package pe.greenminds.ecomind.iam.domain.services;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;

/**
 * Domain port for hashing passwords and comparing a plain password with a stored hash.
 */
public interface PasswordHasher {

  PasswordHash hash(String rawPassword);

  boolean matches(String rawPassword, PasswordHash passwordHash);
}
