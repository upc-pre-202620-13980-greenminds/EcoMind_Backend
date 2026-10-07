package pe.greenminds.ecomind.gamification.infrastructure.acl;

import java.time.Instant;
import java.util.Optional;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.gamification.application.outboundservices.MonetizationServiceClient;

/** Resolves the real supplier when implemented; absence fails explicitly, never as an empty/success response. */
@Component
public class MonetizationServiceClientImpl implements MonetizationServiceClient {
  private final ObjectProvider<MonetizationContextFacade> provider;
  public MonetizationServiceClientImpl(ObjectProvider<MonetizationContextFacade> provider) { this.provider = provider; }
  public Optional<MonetizationContextFacade.ActiveXpMultiplier> getActiveMultiplier(Long userId, Instant at) { return requireSupplier().getActiveMultiplier(userId, at); }
  public void creditRewardGems(MonetizationContextFacade.CreditRewardGems command) { requireSupplier().creditRewardGems(command); }
  public void grantCosmeticReward(MonetizationContextFacade.GrantCosmeticReward command) { requireSupplier().grantCosmeticReward(command); }
  public void requestStreakProtection(MonetizationContextFacade.ConsumeStreakProtector command) { requireSupplier().requestStreakProtection(command); }
  private MonetizationContextFacade requireSupplier() {
    var supplier = provider.getIfAvailable();
    if (supplier == null) throw new IllegalStateException("Monetization integration is not implemented yet");
    return supplier;
  }
}
