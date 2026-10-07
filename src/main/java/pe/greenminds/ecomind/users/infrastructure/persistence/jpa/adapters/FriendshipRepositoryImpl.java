package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FriendshipStatus;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.FriendshipPersistenceEntity;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories.FriendshipPersistenceRepository;

@Repository
public class FriendshipRepositoryImpl implements FriendshipRepository {

  private final FriendshipPersistenceRepository persistenceRepository;

  public FriendshipRepositoryImpl(FriendshipPersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
  }

  @Override
  public Friendship save(Friendship friendship) {
    FriendshipPersistenceEntity entity;
    if (friendship.getId() == null) {
      entity = new FriendshipPersistenceEntity();
      entity.setRequesterId(friendship.getRequesterId().value());
      entity.setReceiverId(friendship.getReceiverId().value());
    } else {
      entity = persistenceRepository.findById(friendship.getId()).orElseThrow();
    }
    entity.setStatus(friendship.getStatus().name());
    return toDomain(persistenceRepository.saveAndFlush(entity));
  }

  @Override
  public void delete(Friendship friendship) {
    persistenceRepository.deleteById(friendship.getId());
    // The pair of users is unique: the delete must reach the database before a new insert.
    persistenceRepository.flush();
  }

  @Override
  public Optional<Friendship> findById(Long friendshipId) {
    return persistenceRepository.findById(friendshipId).map(FriendshipRepositoryImpl::toDomain);
  }

  @Override
  public List<Friendship> findAll() {
    return persistenceRepository.findAll().stream().map(FriendshipRepositoryImpl::toDomain).toList();
  }

  @Override
  public List<Friendship> findByUserId(UserId userId) {
    return persistenceRepository
        .findByRequesterIdOrReceiverId(userId.value(), userId.value())
        .stream()
        .map(FriendshipRepositoryImpl::toDomain)
        .toList();
  }

  @Override
  public Optional<Friendship> findBetween(UserId firstUserId, UserId secondUserId) {
    return persistenceRepository
        .findByRequesterIdAndReceiverId(firstUserId.value(), secondUserId.value())
        .or(
            () ->
                persistenceRepository.findByRequesterIdAndReceiverId(
                    secondUserId.value(), firstUserId.value()))
        .map(FriendshipRepositoryImpl::toDomain);
  }

  private static Friendship toDomain(FriendshipPersistenceEntity entity) {
    return new Friendship(
        entity.getId(),
        new UserId(entity.getRequesterId()),
        new UserId(entity.getReceiverId()),
        FriendshipStatus.valueOf(entity.getStatus()),
        entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toInstant());
  }
}
