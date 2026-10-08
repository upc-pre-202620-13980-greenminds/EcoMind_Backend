package pe.greenminds.ecomind.users.domain.model.aggregates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.users.domain.model.entities.FamilyMember;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Family group created by a parent. It owns its list of members.
 */
public class Family {

  private final FamilyId id;
  private final String name;
  private final String commitment;
  private final List<FamilyMember> members;

  /** Rebuilds a family that already exists; the id is null until it is persisted. */
  public Family(FamilyId id, String name, String commitment, List<FamilyMember> members) {
    this.id = id;
    this.name = name;
    this.commitment = commitment;
    this.members = new ArrayList<>(members);
  }

  /** The parent who creates the family is its first member. */
  public static Family create(UserId parentUserId, String name, String commitment) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Family name is required");
    }
    if (commitment == null || commitment.isBlank()) {
      throw new IllegalArgumentException("Family commitment is required");
    }
    Family family = new Family(null, name.trim(), commitment.trim(), List.of());
    family.members.add(new FamilyMember(null, parentUserId, FamilyRole.PARENT));
    return family;
  }

  public void addMember(UserId userId, FamilyRole familyRole) {
    if (hasMember(userId)) {
      throw new IllegalStateException("The user is already a member of this family");
    }
    members.add(new FamilyMember(null, userId, familyRole));
  }

  public void removeMember(Long familyMemberId) {
    members.removeIf(member -> familyMemberId.equals(member.getId()));
  }

  public Optional<FamilyMember> findMember(Long familyMemberId) {
    return members.stream().filter(member -> familyMemberId.equals(member.getId())).findFirst();
  }

  public Optional<FamilyMember> findMemberByUser(UserId userId) {
    return members.stream().filter(member -> member.getUserId().equals(userId)).findFirst();
  }

  public boolean hasMember(UserId userId) {
    return findMemberByUser(userId).isPresent();
  }

  /** Only the parents of the family can add or remove members. */
  public boolean isManagedBy(UserId userId) {
    return findMemberByUser(userId).map(FamilyMember::isParent).orElse(false);
  }

  public FamilyId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCommitment() {
    return commitment;
  }

  public List<FamilyMember> getMembers() {
    return List.copyOf(members);
  }
}
