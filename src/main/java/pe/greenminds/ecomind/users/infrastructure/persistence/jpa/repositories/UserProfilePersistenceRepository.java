package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.UserProfilePersistenceEntity;

public interface UserProfilePersistenceRepository
    extends JpaRepository<UserProfilePersistenceEntity, Long> {

  Optional<UserProfilePersistenceEntity> findByInvitationCode(String invitationCode);

  boolean existsByInvitationCode(String invitationCode);

  List<UserProfilePersistenceEntity> findByInvitationCodeIsNull();
}
