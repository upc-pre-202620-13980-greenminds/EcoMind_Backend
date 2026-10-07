package pe.greenminds.ecomind.monetization.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.queryservices.GemWalletQueryService;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;
import pe.greenminds.ecomind.monetization.domain.repositories.GemMovementRepository;
import pe.greenminds.ecomind.monetization.domain.repositories.GemWalletRepository;

@Service
public class GemWalletQueryServiceImpl implements GemWalletQueryService {
  private final GemWalletRepository wallets;
  private final GemMovementRepository movements;

  public GemWalletQueryServiceImpl(GemWalletRepository wallets, GemMovementRepository movements) {
    this.wallets = wallets;
    this.movements = movements;
  }

  @Override
  @Transactional(readOnly = true)
  public GemWallet getWallet(Long userId) {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    return wallets.findByUserId(userId).orElse(new GemWallet(userId, 0));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GemMovement> getRecentMovements(Long userId) {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    return movements.findRecentByUserId(userId);
  }
}
