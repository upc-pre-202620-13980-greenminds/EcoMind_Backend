package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.entities.FamilyMember;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.FamilyMemberPersistenceEntity;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities.FamilyPersistenceEntity;
import pe.greenminds.ecomind.users.infrastructure.persistence.jpa.repositories.FamilyPersistenceRepository;

@Repository
public class FamilyRepositoryImpl implements FamilyRepository {

  private final FamilyPersistenceRepository persistenceRepository;

  public FamilyRepositoryImpl(FamilyPersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
  }

  @Override
  public Family save(Family family) {
    FamilyPersistenceEntity entity;
    if (family.getId() == null) {
      entity = new FamilyPersistenceEntity();
      entity.setName(family.getName());
      entity.setCommitment(family.getCommitment());
    } else {
      entity = persistenceRepository.findById(family.getId().value()).orElseThrow();
    }
    synchronizeMembers(family, entity);
    // Flushed so the new members have their generated ids when the family is returned.
    return toDomain(persistenceRepository.saveAndFlush(entity));
  }

  @Override
  public Optional<Family> findById(FamilyId familyId) {
    return persistenceRepository.findById(familyId.value()).map(FamilyRepositoryImpl::toDomain);
  }

  @Override
  public List<Family> findAll() {
    return persistenceRepository.findAll().stream().map(FamilyRepositoryImpl::toDomain).toList();
  }

  @Override
  public Optional<Family> findByMemberUserId(UserId userId) {
    return persistenceRepository
        .findByMembersUserId(userId.value())
        .map(FamilyRepositoryImpl::toDomain);
  }

  @Override
  public Optional<Family> findByMemberId(Long familyMemberId) {
    return persistenceRepository
        .findByMembersId(familyMemberId)
        .map(FamilyRepositoryImpl::toDomain);
  }

  @Override
  public boolean existsByMemberUserId(UserId userId) {
    return persistenceRepository.existsByMembersUserId(userId.value());
  }

  /** Removes the members that left the family and adds the ones that do not have an id yet. */
  private static void synchronizeMembers(Family family, FamilyPersistenceEntity entity) {
    Set<Long> remainingIds =
        family.getMembers().stream()
            .map(FamilyMember::getId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    entity.getMembers().removeIf(member -> !remainingIds.contains(member.getId()));

    family.getMembers().stream()
        .filter(member -> member.getId() == null)
        .forEach(
            member -> {
              FamilyMemberPersistenceEntity memberEntity = new FamilyMemberPersistenceEntity();
              memberEntity.setFamily(entity);
              memberEntity.setUserId(member.getUserId().value());
              memberEntity.setFamilyRole(member.getFamilyRole().name());
              entity.getMembers().add(memberEntity);
            });
  }

  private static Family toDomain(FamilyPersistenceEntity entity) {
    List<FamilyMember> members =
        entity.getMembers().stream()
            .map(
                member ->
                    new FamilyMember(
                        member.getId(),
                        new UserId(member.getUserId()),
                        FamilyRole.valueOf(member.getFamilyRole())))
            .toList();
    return new Family(
        new FamilyId(entity.getId()), entity.getName(), entity.getCommitment(), members);
  }
}
