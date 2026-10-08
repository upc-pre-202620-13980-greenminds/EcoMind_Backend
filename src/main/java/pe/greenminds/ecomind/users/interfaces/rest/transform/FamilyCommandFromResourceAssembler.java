package pe.greenminds.ecomind.users.interfaces.rest.transform;

import pe.greenminds.ecomind.users.domain.model.commands.AddFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.commands.CreateFamilyCommand;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.AddFamilyMemberResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.CreateFamilyResource;

public final class FamilyCommandFromResourceAssembler {

  private FamilyCommandFromResourceAssembler() {
  }

  public static CreateFamilyCommand toCommandFromResource(
      Long parentUserId, CreateFamilyResource resource) {
    return new CreateFamilyCommand(
        new UserId(parentUserId), resource.name(), resource.commitment());
  }

  public static AddFamilyMemberCommand toCommandFromResource(
      Long requestedBy, AddFamilyMemberResource resource) {
    return new AddFamilyMemberCommand(
        new UserId(requestedBy),
        new FamilyId(resource.familyId()),
        new UserId(resource.userId()),
        FamilyRole.valueOf(resource.familyRole()));
  }
}
