package pe.greenminds.ecomind.users.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public interface UserProfileRepository {

  UserProfile save(UserProfile userProfile);

  Optional<UserProfile> findById(UserId userId);

  List<UserProfile> findAll();

  boolean existsById(UserId userId);
}
