package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.entities.EmailVerification;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.PasswordHash;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.EmailVerificationPersistenceEntity;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.PendingRegistrationPersistenceEntity;

public final class PendingRegistrationPersistenceAssembler {

  private PendingRegistrationPersistenceAssembler() {
  }

  public static PendingRegistration toDomainFromPersistence(
      PendingRegistrationPersistenceEntity entity) {
    EmailVerificationPersistenceEntity verification = entity.getEmailVerification();
    return new PendingRegistration(
        entity.getId(),
        entity.getName(),
        new EmailAddress(entity.getEmail()),
        new PasswordHash(entity.getPasswordHash()),
        SocialRole.valueOf(entity.getSocialRole()),
        entity.getExpiresAt(),
        entity.getCreatedAt(),
        new EmailVerification(
            new TokenHash(verification.getTokenHash()),
            verification.getExpiresAt(),
            verification.getUsedAt(),
            verification.getAttempts()));
  }

  public static PendingRegistrationPersistenceEntity toPersistenceFromDomain(
      PendingRegistration pendingRegistration) {
    PendingRegistrationPersistenceEntity entity = new PendingRegistrationPersistenceEntity();
    entity.setName(pendingRegistration.getName());
    entity.setEmail(pendingRegistration.getEmail().value());
    entity.setPasswordHash(pendingRegistration.getPasswordHash().value());
    entity.setSocialRole(pendingRegistration.getSocialRole().name());
    entity.setExpiresAt(pendingRegistration.getExpiresAt());
    entity.setCreatedAt(pendingRegistration.getCreatedAt());

    EmailVerification verification = pendingRegistration.getEmailVerification();
    EmailVerificationPersistenceEntity verificationEntity =
        new EmailVerificationPersistenceEntity();
    verificationEntity.setPendingRegistration(entity);
    verificationEntity.setTokenHash(verification.getTokenHash().value());
    verificationEntity.setExpiresAt(verification.getExpiresAt());
    entity.setEmailVerification(verificationEntity);
    copyMutableState(pendingRegistration, entity);
    return entity;
  }

  /** Copies what can change after creation: the use and the attempts of the verification. */
  public static void copyMutableState(
      PendingRegistration pendingRegistration, PendingRegistrationPersistenceEntity entity) {
    EmailVerification verification = pendingRegistration.getEmailVerification();
    entity.getEmailVerification().setUsedAt(verification.getUsedAt());
    entity.getEmailVerification().setAttempts(verification.getAttempts());
  }
}
