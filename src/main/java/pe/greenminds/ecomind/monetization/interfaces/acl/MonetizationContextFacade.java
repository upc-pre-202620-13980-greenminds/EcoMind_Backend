package pe.greenminds.ecomind.monetization.interfaces.acl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Public contract to implement with real wallet, inventory and multiplier services. No stub bean is registered. */
public interface MonetizationContextFacade {
  record ActiveXpMultiplier(UUID id, BigDecimal experienceFactor, Instant startsAt, Instant expiresAt) {
    public ActiveXpMultiplier {
      Objects.requireNonNull(id); Objects.requireNonNull(experienceFactor);
      Objects.requireNonNull(startsAt); Objects.requireNonNull(expiresAt);
      if (experienceFactor.compareTo(BigDecimal.ONE) < 0 || !startsAt.isBefore(expiresAt))
        throw new IllegalArgumentException("Invalid multiplier factor or validity period");
    }
    public boolean isActiveAt(Instant instant) {
      return !instant.isBefore(startsAt) && instant.isBefore(expiresAt);
    }
  }
  record CreditRewardGems(UUID rewardId, Long userId, int gems) {
    public CreditRewardGems {
      Objects.requireNonNull(rewardId); requireUser(userId);
      if (gems <= 0) throw new IllegalArgumentException("Gem credit must be positive");
    }
  }
  record GrantCosmeticReward(UUID awardId, Long userId, UUID cosmeticId) {
    public GrantCosmeticReward {
      Objects.requireNonNull(awardId); requireUser(userId); Objects.requireNonNull(cosmeticId);
    }
  }
  record ConsumeStreakProtector(UUID requestId, Long userId, LocalDate streakDate) {
    public ConsumeStreakProtector {
      Objects.requireNonNull(requestId); requireUser(userId); Objects.requireNonNull(streakDate);
    }
  }

  /** Empty means verified absence of an active multiplier. Technical failure must propagate. */
  Optional<ActiveXpMultiplier> getActiveMultiplier(Long userId, Instant at);
  /** Deduplicate by rewardId; persist credit before reporting success. */
  void creditRewardGems(CreditRewardGems command);
  /** Deduplicate by awardId. */
  void grantCosmeticReward(GrantCosmeticReward command);
  /** Publish a correlated protected/unavailable result after processing. Technical failure is not unavailable inventory. */
  void requestStreakProtection(ConsumeStreakProtector command);

  private static void requireUser(Long userId) {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
  }
}
