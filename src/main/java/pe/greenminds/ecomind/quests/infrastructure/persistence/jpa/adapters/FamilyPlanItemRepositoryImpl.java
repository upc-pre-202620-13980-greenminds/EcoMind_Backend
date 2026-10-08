package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.FamilyPlanItemPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.FamilyPlanItemPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class FamilyPlanItemRepositoryImpl implements FamilyPlanItemRepository {
    private final FamilyPlanItemPersistenceRepository familyPlanItemPersistenceRepository;

    public FamilyPlanItemRepositoryImpl(
            FamilyPlanItemPersistenceRepository familyPlanItemPersistenceRepository
    ) {
        this.familyPlanItemPersistenceRepository = familyPlanItemPersistenceRepository;
    }

    @Override
    public FamilyPlanItem save(FamilyPlanItem familyPlanItem) {
        return FamilyPlanItemPersistenceAssembler.toDomainFromPersistence(
                familyPlanItemPersistenceRepository.save(
                        FamilyPlanItemPersistenceAssembler.toPersistenceFromDomain(
                                familyPlanItem
                        )
                )
        );
    }

    @Override
    public List<FamilyPlanItem> findByFamilyPlanId(Long familyPlanId) {
        return familyPlanItemPersistenceRepository.findByFamilyPlanId(familyPlanId)
                .stream()
                .map(FamilyPlanItemPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<FamilyPlanItem> findByCollaborativeSessionId(
            Long collaborativeSessionId
    ) {
        return familyPlanItemPersistenceRepository
                .findByCollaborativeSessionId(collaborativeSessionId)
                .map(FamilyPlanItemPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByCollaborativeSessionId(Long collaborativeSessionId) {
        return familyPlanItemPersistenceRepository.existsByCollaborativeSessionId(
                collaborativeSessionId
        );
    }

    @Override
    public void deleteByFamilyPlanId(Long familyPlanId) {
        familyPlanItemPersistenceRepository.deleteByFamilyPlanId(familyPlanId);
    }
}
