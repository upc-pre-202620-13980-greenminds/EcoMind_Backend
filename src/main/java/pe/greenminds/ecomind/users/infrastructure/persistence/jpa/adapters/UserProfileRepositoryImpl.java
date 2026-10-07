package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.UserProfilePersistenceEntity;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories.UserProfilePersistenceRepository;

@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {

  private final UserProfilePersistenceRepository persistenceRepository;

  public UserProfileRepositoryImpl(UserProfilePersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
  }

  @Override
  public UserProfile save(UserProfile userProfile) {
    UserProfilePersistenceEntity entity =
        persistenceRepository
            .findById(userProfile.getUserId().value())
            .orElseGet(UserProfilePersistenceEntity::new);
    entity.setUserId(userProfile.getUserId().value());
    entity.setName(userProfile.getName());
    entity.setSocialRole(userProfile.getSocialRole().name());
    entity.setStreak(userProfile.getStreak());
    entity.setLastStreakDate(userProfile.getLastStreakDate());
    entity.setEcopoints(userProfile.getEcopoints());
    entity.setGemBalance(userProfile.getGemBalance());
    entity.setEquippedCosmeticId(userProfile.getEquippedCosmeticId());
    return toDomain(persistenceRepository.save(entity));
  }

  @Override
  public Optional<UserProfile> findById(UserId userId) {
    return persistenceRepository.findById(userId.value()).map(UserProfileRepositoryImpl::toDomain);
  }

  @Override
  public List<UserProfile> findAll() {
    return persistenceRepository.findAll().stream().map(UserProfileRepositoryImpl::toDomain).toList();
  }

  @Override
  public boolean existsById(UserId userId) {
    return persistenceRepository.existsById(userId.value());
  }

  private static UserProfile toDomain(UserProfilePersistenceEntity entity) {
    return new UserProfile(
        new UserId(entity.getUserId()),
        entity.getName(),
        SocialRole.valueOf(entity.getSocialRole()),
        entity.getStreak(),
        entity.getLastStreakDate(),
        entity.getEcopoints(),
        entity.getGemBalance(),
        entity.getEquippedCosmeticId());
  }
}
