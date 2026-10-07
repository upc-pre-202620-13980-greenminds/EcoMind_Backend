package pe.greenminds.ecomind.monetization.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.queryservices.GemWalletQueryService;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;
import pe.greenminds.ecomind.monetization.domain.repositories.GemMovementRepository;
import pe.greenminds.ecomind.monetization.application.outboundservices.UserGemBalanceGateway;

@Service
public class GemWalletQueryServiceImpl implements GemWalletQueryService {
  private final UserGemBalanceGateway balances;
  private final GemMovementRepository movements;

  public GemWalletQueryServiceImpl(UserGemBalanceGateway balances, GemMovementRepository movements) {
    this.balances = balances;
    this.movements = movements;
  }

  @Override
  @Transactional(readOnly = true)
  public GemWallet getWallet(Long userId) {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    return new GemWallet(userId, balances.getBalance(userId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GemMovement> getRecentMovements(Long userId) {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    return movements.findRecentByUserId(userId);
  }
}
