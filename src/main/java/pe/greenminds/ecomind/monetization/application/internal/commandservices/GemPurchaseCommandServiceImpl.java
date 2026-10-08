package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.commandservices.GemPurchaseCommandService;
import pe.greenminds.ecomind.monetization.application.commandservices.GemWalletCommandService;
import pe.greenminds.ecomind.monetization.application.outboundservices.payment.ChargeRequest;
import pe.greenminds.ecomind.monetization.application.outboundservices.payment.PaymentGatewayResolver;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentStatus;
import pe.greenminds.ecomind.monetization.domain.repositories.GemPackageRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemPurchasePersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.GemPurchasePersistenceRepository;

@Service
public class GemPurchaseCommandServiceImpl implements GemPurchaseCommandService {
  private final GemPackageRepository packages;
  private final GemPurchasePersistenceRepository purchases;
  private final PaymentGatewayResolver gateways;
  private final GemWalletCommandService wallet;

  public GemPurchaseCommandServiceImpl(
      GemPackageRepository packages,
      GemPurchasePersistenceRepository purchases,
      PaymentGatewayResolver gateways,
      GemWalletCommandService wallet) {
    this.packages = packages;
    this.purchases = purchases;
    this.gateways = gateways;
    this.wallet = wallet;
  }

  @Transactional
  @Override
  public GemPurchasePersistenceEntity checkout(
      Long userId, UUID packageId, PaymentMethodType method, UUID requestId) {
    var replay = purchases.findByRequestId(requestId.toString());
    if (replay.isPresent()) {
      var purchase = replay.get();
      if (!purchase.getUserId().equals(userId)
          || !purchase.getPackageId().equals(packageId.toString())
          || purchase.getPaymentMethod() != method) {
        throw new IllegalArgumentException("Request id was already used for another checkout");
      }
      return purchase;
    }

    var gemPackage = packages.findById(packageId).filter(item -> item.active())
        .orElseThrow(() -> new IllegalArgumentException("Gem package is unavailable"));
    var purchase = new GemPurchasePersistenceEntity();
    purchase.setId(UUID.randomUUID().toString());
    purchase.setRequestId(requestId.toString());
    purchase.setUserId(userId);
    purchase.setPackageId(packageId.toString());
    purchase.setGemAmount(gemPackage.gemAmount());
    purchase.setAmountPaid(gemPackage.price());
    purchase.setCurrency(gemPackage.currency());
    purchase.setPaymentMethod(method);
    purchase.setPaymentStatus(PaymentStatus.PENDING);
    purchase.setCreatedAt(Instant.now());
    return purchases.save(purchase);
  }

  @Transactional
  @Override
  public GemPurchasePersistenceEntity pay(
      Long userId, UUID purchaseId, String sourceToken, String email) {
    var purchase = ownPurchase(userId, purchaseId);
    if (purchase.getPaymentStatus() == PaymentStatus.APPROVED) return purchase;
    if (purchase.getPaymentStatus() == PaymentStatus.REJECTED) {
      throw new IllegalArgumentException("Rejected purchases cannot be paid again");
    }

    int amountInCents = purchase.getAmountPaid().movePointRight(2)
        .setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    var result = gateways.require(purchase.getPaymentMethod()).charge(new ChargeRequest(
        amountInCents, purchase.getCurrency(), email, sourceToken,
        "EcoMind - " + purchase.getGemAmount() + " gems"));
    if (!result.approved()) {
      purchase.setPaymentStatus(PaymentStatus.REJECTED);
      purchase.setCompletedAt(Instant.now());
      return purchases.save(purchase);
    }
    if (result.reference() == null || result.reference().isBlank()) {
      throw new IllegalStateException("Approved payment has no provider reference");
    }

    wallet.credit(
        userId, purchase.getGemAmount(), GemMovementType.GEM_PURCHASE_CREDIT,
        GemMovementOrigin.GEM_PACKAGE, UUID.fromString(purchase.getId()));
    purchase.setPaymentReference(result.reference());
    purchase.setPaymentStatus(PaymentStatus.APPROVED);
    purchase.setCompletedAt(Instant.now());
    return purchases.save(purchase);
  }

  @Transactional(readOnly = true)
  @Override
  public List<GemPurchasePersistenceEntity> findMine(Long userId) {
    return purchases.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  @Override
  public GemPurchasePersistenceEntity findMineById(Long userId, UUID purchaseId) {
    return ownPurchase(userId, purchaseId);
  }

  private GemPurchasePersistenceEntity ownPurchase(Long userId, UUID purchaseId) {
    var purchase = purchases.findById(purchaseId.toString())
        .orElseThrow(() -> new IllegalArgumentException("Gem purchase was not found"));
    if (!purchase.getUserId().equals(userId)) {
      throw new IllegalArgumentException("Gem purchase was not found");
    }
    return purchase;
  }
}
