package pe.greenminds.ecomind.monetization.infrastructure.acl;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.monetization.application.outboundservices.UserGemBalanceGateway;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.InsufficientGemBalanceException;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@Component
public class UserGemBalanceGatewayImpl implements UserGemBalanceGateway {
  private final UserProfileRepository profiles;

  public UserGemBalanceGatewayImpl(UserProfileRepository profiles) {
    this.profiles = profiles;
  }

  @Override public int getBalance(Long userId) { return profile(userId).getGemBalance(); }

  @Override public int credit(Long userId, int amount) {
    if (amount <= 0) throw new IllegalArgumentException("Gem amount must be positive");
    var profile = profile(userId);
    int balance = Math.addExact(profile.getGemBalance(), amount);
    profile.updateProgress(profile.getStreak(), profile.getLastStreakDate(), profile.getEcopoints(), balance);
    profiles.save(profile);
    return balance;
  }

  @Override public int debit(Long userId, int amount) {
    if (amount <= 0) throw new IllegalArgumentException("Gem amount must be positive");
    var profile = profile(userId);
    if (amount > profile.getGemBalance())
      throw new InsufficientGemBalanceException(profile.getGemBalance(), amount);
    int balance = profile.getGemBalance() - amount;
    profile.updateProgress(profile.getStreak(), profile.getLastStreakDate(), profile.getEcopoints(), balance);
    profiles.save(profile);
    return balance;
  }

  private pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile profile(Long userId) {
    return profiles.findById(new UserId(userId))
        .orElseThrow(() -> new IllegalArgumentException("User profile not found"));
  }
}
