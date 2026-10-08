package pe.greenminds.ecomind.users.domain.services;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;

/**
 * A user can belong to one family at a time.
 */
@Service
public class FamilyMembershipPolicy {

  private final FamilyRepository familyRepository;

  public FamilyMembershipPolicy(FamilyRepository familyRepository) {
    this.familyRepository = familyRepository;
  }

  public boolean canJoinAFamily(UserId userId) {
    return !familyRepository.existsByMemberUserId(userId);
  }
}
