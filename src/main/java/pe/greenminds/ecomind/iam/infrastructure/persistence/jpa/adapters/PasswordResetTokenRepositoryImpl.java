package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.adapters;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;
import pe.greenminds.ecomind.iam.domain.repositories.PasswordResetTokenRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenPersistenceEntity;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories.AccountPersistenceRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenPersistenceRepository;

@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

  private final PasswordResetTokenPersistenceRepository persistenceRepository;
  private final AccountPersistenceRepository accountPersistenceRepository;

  public PasswordResetTokenRepositoryImpl(
      PasswordResetTokenPersistenceRepository persistenceRepository,
      AccountPersistenceRepository accountPersistenceRepository) {
    this.persistenceRepository = persistenceRepository;
    this.accountPersistenceRepository = accountPersistenceRepository;
  }

  @Override
  public PasswordResetToken save(PasswordResetToken passwordResetToken) {
    PasswordResetTokenPersistenceEntity entity;
    if (passwordResetToken.getId() == null) {
      entity = new PasswordResetTokenPersistenceEntity();
      entity.setAccount(
          accountPersistenceRepository.getReferenceById(passwordResetToken.getAccountId().value()));
      entity.setTokenHash(passwordResetToken.getTokenHash().value());
      entity.setExpiresAt(passwordResetToken.getExpiresAt());
    } else {
      entity = persistenceRepository.findById(passwordResetToken.getId()).orElseThrow();
    }
    entity.setUsedAt(passwordResetToken.getUsedAt());
    return toDomain(persistenceRepository.save(entity));
  }

  @Override
  public Optional<PasswordResetToken> findByTokenHash(TokenHash tokenHash) {
    return persistenceRepository
        .findByTokenHash(tokenHash.value())
        .map(PasswordResetTokenRepositoryImpl::toDomain);
  }

  private static PasswordResetToken toDomain(PasswordResetTokenPersistenceEntity entity) {
    return new PasswordResetToken(
        entity.getId(),
        new AccountId(entity.getAccount().getId()),
        new TokenHash(entity.getTokenHash()),
        entity.getExpiresAt(),
        entity.getUsedAt());
  }
}
