package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityAchievement;
import pe.greenminds.ecomind.community.domain.repositories.CommunityAchievementRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.CommunityAchievementPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityAchievementPersistenceRepository;

@Repository
public class CommunityAchievementRepositoryImpl implements CommunityAchievementRepository {
    private final CommunityAchievementPersistenceRepository repository;

    public CommunityAchievementRepositoryImpl(CommunityAchievementPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public CommunityAchievement save(CommunityAchievement communityAchievement) {
        return CommunityAchievementPersistenceAssembler
                .toDomain(repository.save(CommunityAchievementPersistenceAssembler.toEntity(communityAchievement)));
    }

    @Override
    public List<CommunityAchievement> findAll(Long communityId) {
        return (communityId == null ? repository.findAll() : repository.findByCommunityId(communityId)).stream()
                .map(CommunityAchievementPersistenceAssembler::toDomain).toList();
    }
}
