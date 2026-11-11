package pe.greenminds.ecomind.users.domain.model.aggregates;

import java.time.LocalDate;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Profile of a user whose account already exists in IAM: who they are in the platform and the
 * progress shown to them.
 */
public class UserProfile {

  private static final int NAME_MIN_LENGTH = 2;
  private static final int NAME_MAX_LENGTH = 120;

  private final UserId userId;
  private String name;
  private final SocialRole socialRole;
  private int streak;
  private LocalDate lastStreakDate;
  private int ecopoints;
  private int gemBalance;
  private final Long equippedCosmeticId;

  /** Rebuilds a profile that already exists. */
  public UserProfile(
      UserId userId,
      String name,
      SocialRole socialRole,
      int streak,
      LocalDate lastStreakDate,
      int ecopoints,
      int gemBalance,
      Long equippedCosmeticId) {
    this.userId = userId;
    this.name = name;
    this.socialRole = socialRole;
    this.streak = streak;
    this.lastStreakDate = lastStreakDate;
    this.ecopoints = ecopoints;
    this.gemBalance = gemBalance;
    this.equippedCosmeticId = equippedCosmeticId;
  }

  /** A new profile starts without progress. */
  public static UserProfile create(UserId userId, String name, SocialRole socialRole) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Profile name is required");
    }
    return new UserProfile(userId, name.trim(), socialRole, 0, null, 0, 0, null);
  }

  /** Replaces the name shown in the profile; it follows the same length rule as the registration. */
  public void rename(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Profile name is required");
    }
    String trimmed = name.trim();
    if (trimmed.length() < NAME_MIN_LENGTH || trimmed.length() > NAME_MAX_LENGTH) {
      throw new IllegalArgumentException("Profile name must have 2 to 120 characters");
    }
    this.name = trimmed;
  }

  public void updateProgress(int streak, LocalDate lastStreakDate, int ecopoints, int gemBalance) {
    if (streak < 0 || ecopoints < 0 || gemBalance < 0) {
      throw new IllegalArgumentException("Streak, ecopoints and gem balance cannot be negative");
    }
    this.streak = streak;
    this.lastStreakDate = lastStreakDate;
    this.ecopoints = ecopoints;
    this.gemBalance = gemBalance;
  }

  public boolean isParent() {
    return socialRole == SocialRole.PARENT;
  }

  public UserId getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public SocialRole getSocialRole() {
    return socialRole;
  }

  public int getStreak() {
    return streak;
  }

  public LocalDate getLastStreakDate() {
    return lastStreakDate;
  }

  public int getEcopoints() {
    return ecopoints;
  }

  public int getGemBalance() {
    return gemBalance;
  }

  public Long getEquippedCosmeticId() {
    return equippedCosmeticId;
  }
}
