package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.repositories.CommunityGoalRepository;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.CommunityGoalPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityGoalPersistenceRepository;

@Repository
public class CommunityGoalRepositoryImpl implements CommunityGoalRepository {
    private final CommunityGoalPersistenceRepository repository;

    public CommunityGoalRepositoryImpl(CommunityGoalPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public CommunityGoal save(CommunityGoal communityGoal) {
        var persistenceEntity = communityGoal.id() == null
                ? CommunityGoalPersistenceAssembler.toEntity(communityGoal)
                : repository.findById(communityGoal.id()).orElseThrow();
        if (communityGoal.id() != null)
            persistenceEntity.applyProgress(communityGoal.progress(), communityGoal.participants());
        return CommunityGoalPersistenceAssembler.toDomain(repository.save(persistenceEntity));
    }

    @Override
    public Optional<CommunityGoal> findById(Long id) {
        return repository.findById(id).map(CommunityGoalPersistenceAssembler::toDomain);
    }

    @Override
    public List<CommunityGoal> findAll(Long communityId) {
        return (communityId == null ? repository.findAll() : repository.findByCommunityId(communityId)).stream()
                .map(CommunityGoalPersistenceAssembler::toDomain).toList();
    }

    @Override
    public boolean existsActiveByCommunityId(Long communityId) {
        return repository.existsByCommunityIdAndStatus(communityId, CommunityGoalStatus.ACTIVE);
    }
}
