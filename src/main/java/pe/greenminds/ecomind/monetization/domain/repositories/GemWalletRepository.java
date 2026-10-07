package pe.greenminds.ecomind.monetization.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;

public interface GemWalletRepository {
  Optional<GemWallet> findByUserId(Long userId);
  Optional<GemWallet> lockByUserId(Long userId);
  GemWallet save(GemWallet wallet);
}
