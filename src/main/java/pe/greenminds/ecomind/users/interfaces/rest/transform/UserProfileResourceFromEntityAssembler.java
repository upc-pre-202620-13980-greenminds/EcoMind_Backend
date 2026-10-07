package pe.greenminds.ecomind.users.interfaces.rest.transform;

import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.interfaces.rest.resources.UserProfileResource;

public final class UserProfileResourceFromEntityAssembler {

  private UserProfileResourceFromEntityAssembler() {
  }

  public static UserProfileResource toResourceFromEntity(UserProfile profile) {
    return new UserProfileResource(
        profile.getUserId().value(),
        profile.getName(),
        profile.getSocialRole().name(),
        profile.getStreak(),
        profile.getLastStreakDate(),
        profile.getEcopoints(),
        profile.getGemBalance(),
        profile.getEquippedCosmeticId());
  }
}
