package pe.greenminds.ecomind.users.interfaces.rest.transform;

import pe.greenminds.ecomind.users.domain.model.commands.UpdateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.UpdateUserProfileResource;

public final class UpdateProfileCommandFromResourceAssembler {

  private UpdateProfileCommandFromResourceAssembler() {
  }

  public static UpdateProfileCommand toCommandFromResource(
      Long requestedBy, Long userId, UpdateUserProfileResource resource) {
    return new UpdateProfileCommand(
        new UserId(requestedBy),
        new UserId(userId),
        resource.streak(),
        resource.lastStreakDate(),
        resource.ecopoints(),
        resource.gemBalance());
  }
}
