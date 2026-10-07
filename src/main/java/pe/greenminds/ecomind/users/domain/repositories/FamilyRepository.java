package pe.greenminds.ecomind.users.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public interface FamilyRepository {

  /** Saves the family with its members and returns it with the generated ids. */
  Family save(Family family);

  Optional<Family> findById(FamilyId familyId);

  List<Family> findAll();

  Optional<Family> findByMemberUserId(UserId userId);

  Optional<Family> findByMemberId(Long familyMemberId);

  boolean existsByMemberUserId(UserId userId);
}
