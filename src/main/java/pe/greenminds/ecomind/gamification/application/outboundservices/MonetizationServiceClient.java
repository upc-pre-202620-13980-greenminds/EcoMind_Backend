package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade;

import java.time.Instant;
import java.util.Optional;

/** Uses only the supplier public contract, never its internal model. */
public interface MonetizationServiceClient {
    Optional<MonetizationContextFacade.ActiveXpMultiplier> getActiveMultiplier(
            Long userId, Instant at);

    void creditRewardGems(MonetizationContextFacade.CreditRewardGems command);

    void grantCosmeticReward(MonetizationContextFacade.GrantCosmeticReward command);

    void requestStreakProtection(MonetizationContextFacade.ConsumeStreakProtector command);
}
