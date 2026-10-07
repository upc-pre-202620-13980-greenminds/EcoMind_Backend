package pe.greenminds.ecomind.users.interfaces.rest.transform;

import java.util.List;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.entities.FamilyMember;
import pe.greenminds.ecomind.users.interfaces.rest.resources.FamilyMemberResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.FamilyResource;

public final class FamilyResourceFromEntityAssembler {

  private FamilyResourceFromEntityAssembler() {
  }

  public static FamilyResource toResourceFromEntity(Family family) {
    return new FamilyResource(
        family.getId().value(),
        family.getName(),
        family.getCommitment(),
        toMemberResourcesFromEntity(family));
  }

  public static List<FamilyMemberResource> toMemberResourcesFromEntity(Family family) {
    return family.getMembers().stream()
        .map(member -> toMemberResourceFromEntity(family, member))
        .toList();
  }

  public static FamilyMemberResource toMemberResourceFromEntity(
      Family family, FamilyMember member) {
    return new FamilyMemberResource(
        member.getId(),
        family.getId().value(),
        member.getUserId().value(),
        member.getFamilyRole().name());
  }
}
