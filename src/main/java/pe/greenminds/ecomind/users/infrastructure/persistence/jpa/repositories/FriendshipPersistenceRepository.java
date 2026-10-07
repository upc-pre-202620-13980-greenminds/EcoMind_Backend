package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.FriendshipPersistenceEntity;

public interface FriendshipPersistenceRepository
    extends JpaRepository<FriendshipPersistenceEntity, Long> {

  List<FriendshipPersistenceEntity> findByRequesterIdOrReceiverId(Long requesterId, Long receiverId);

  Optional<FriendshipPersistenceEntity> findByRequesterIdAndReceiverId(
      Long requesterId, Long receiverId);
}
