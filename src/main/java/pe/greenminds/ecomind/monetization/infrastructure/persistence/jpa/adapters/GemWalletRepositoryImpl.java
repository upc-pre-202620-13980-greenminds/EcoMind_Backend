package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;
import pe.greenminds.ecomind.monetization.domain.repositories.GemWalletRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemWalletPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.GemWalletPersistenceRepository;

@Repository
public class GemWalletRepositoryImpl implements GemWalletRepository {
  private final GemWalletPersistenceRepository persistence;

  public GemWalletRepositoryImpl(GemWalletPersistenceRepository persistence) {
    this.persistence = persistence;
  }

  @Override
  public Optional<GemWallet> findByUserId(Long userId) {
    return persistence.findById(userId).map(GemWalletRepositoryImpl::toDomain);
  }

  @Override
  public Optional<GemWallet> lockByUserId(Long userId) {
    return persistence.lockByUserId(userId).map(GemWalletRepositoryImpl::toDomain);
  }

  @Override
  public GemWallet save(GemWallet wallet) {
    var entity = persistence.findById(wallet.userId()).orElseGet(GemWalletPersistenceEntity::new);
    entity.setUserId(wallet.userId());
    entity.setBalance(wallet.balance());
    return toDomain(persistence.save(entity));
  }

  private static GemWallet toDomain(GemWalletPersistenceEntity entity) {
    return new GemWallet(entity.getUserId(), entity.getBalance());
  }
}
