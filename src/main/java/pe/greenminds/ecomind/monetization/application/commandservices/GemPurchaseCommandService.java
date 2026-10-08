package pe.greenminds.ecomind.monetization.application.commandservices;

import java.util.List;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPurchasePersistenceEntity;

public interface GemPurchaseCommandService {
  GemPurchasePersistenceEntity checkout(
      Long userId, UUID packageId, PaymentMethodType method, UUID requestId);

  GemPurchasePersistenceEntity pay(
      Long userId, UUID purchaseId, String sourceToken, String email);

  List<GemPurchasePersistenceEntity> findMine(Long userId);

  GemPurchasePersistenceEntity findMineById(Long userId, UUID purchaseId);
}
