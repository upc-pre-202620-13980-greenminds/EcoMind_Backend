package pe.greenminds.ecomind.users.interfaces.rest.transform;

import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.UserProfileResource;

public final class UserProfileResourceFromEntityAssembler {

  private UserProfileResourceFromEntityAssembler() {
  }

  /** The invitation code is only shown to the owner of the profile. */
  public static UserProfileResource toResourceFromEntity(UserProfile profile, UserId viewer) {
    return new UserProfileResource(
        profile.getUserId().value(),
        profile.getName(),
        profile.getSocialRole().name(),
        profile.getStreak(),
        profile.getLastStreakDate(),
        profile.getEcopoints(),
        profile.getGemBalance(),
        profile.getEquippedCosmeticId(),
        invitationCodeFor(profile, viewer),
        null);
  }

  /** Null unless the user who asks is the owner of the profile. */
  public static String invitationCodeFor(UserProfile profile, UserId viewer) {
    if (viewer == null || !profile.isOwnedBy(viewer) || profile.getInvitationCode() == null) {
      return null;
    }
    return profile.getInvitationCode().value();
  }
}
