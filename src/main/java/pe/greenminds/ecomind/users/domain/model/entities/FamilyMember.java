package pe.greenminds.ecomind.users.domain.model.entities;

import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Link between a user and a family, with the role the user has in the group.
 */
public class FamilyMember {

  private final Long id;
  private final UserId userId;
  private final FamilyRole familyRole;

  /** The id is null until the member is persisted. */
  public FamilyMember(Long id, UserId userId, FamilyRole familyRole) {
    this.id = id;
    this.userId = userId;
    this.familyRole = familyRole;
  }

  public boolean isParent() {
    return familyRole == FamilyRole.PARENT;
  }

  public Long getId() {
    return id;
  }

  public UserId getUserId() {
    return userId;
  }

  public FamilyRole getFamilyRole() {
    return familyRole;
  }
}
