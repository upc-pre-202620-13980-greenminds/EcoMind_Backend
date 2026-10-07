package pe.greenminds.ecomind.monetization.application.queryservices;

import java.util.List;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;

public interface GemWalletQueryService {
  GemWallet getWallet(Long userId);
  List<GemMovement> getRecentMovements(Long userId);
}
