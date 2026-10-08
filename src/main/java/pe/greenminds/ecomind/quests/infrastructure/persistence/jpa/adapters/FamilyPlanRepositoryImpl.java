package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlan;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.FamilyPlanPersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.FamilyPlanPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class FamilyPlanRepositoryImpl implements FamilyPlanRepository {
    private final FamilyPlanPersistenceRepository familyPlanPersistenceRepository;

    public FamilyPlanRepositoryImpl(
            FamilyPlanPersistenceRepository familyPlanPersistenceRepository
    ) {
        this.familyPlanPersistenceRepository = familyPlanPersistenceRepository;
    }

    @Override
    public FamilyPlan save(FamilyPlan familyPlan) {
        var savedPlan = FamilyPlanPersistenceAssembler.toDomainFromPersistence(
                familyPlanPersistenceRepository.save(
                        FamilyPlanPersistenceAssembler.toPersistenceFromDomain(familyPlan)
                )
        );
        return savedPlan;
    }

    @Override
    public Optional<FamilyPlan> findById(Long id) {
        return familyPlanPersistenceRepository.findById(id)
                .map(FamilyPlanPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<FamilyPlan> findByFamilyId(Long familyId) {
        return familyPlanPersistenceRepository.findByFamilyId(familyId)
                .stream()
                .map(FamilyPlanPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<FamilyPlan> findByFamilyIdAndStatus(
            Long familyId,
            FamilyPlanStatus status
    ) {
        return familyPlanPersistenceRepository
                .findFirstByFamilyIdAndStatusOrderByIdDesc(familyId, status)
                .map(FamilyPlanPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public void deleteById(Long id) {
        familyPlanPersistenceRepository.deleteById(id);
    }
}
