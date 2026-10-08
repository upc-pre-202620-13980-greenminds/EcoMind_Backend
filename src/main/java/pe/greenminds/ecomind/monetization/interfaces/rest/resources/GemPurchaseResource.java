package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentStatus;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPurchasePersistenceEntity;

public record GemPurchaseResource(
    UUID id,
    UUID packageId,
    int gemAmount,
    BigDecimal amountPaid,
    String currency,
    PaymentMethodType paymentMethod,
    PaymentStatus paymentStatus,
    String paymentReference,
    Instant createdAt,
    Instant completedAt) {
  public static GemPurchaseResource from(GemPurchasePersistenceEntity entity) {
    return new GemPurchaseResource(
        UUID.fromString(entity.getId()), UUID.fromString(entity.getPackageId()),
        entity.getGemAmount(), entity.getAmountPaid(), entity.getCurrency(),
        entity.getPaymentMethod(), entity.getPaymentStatus(), entity.getPaymentReference(),
        entity.getCreatedAt(), entity.getCompletedAt());
  }
}
