package pe.greenminds.ecomind.monetization.application.commandservices;

import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;

public interface GemWalletCommandService {
  GemWallet credit(Long userId, int amount, GemMovementType type,
      GemMovementOrigin origin, UUID referenceId);
  GemWallet debit(Long userId, int amount, GemMovementOrigin origin, UUID referenceId);
}
