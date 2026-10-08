package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityGoal;
import pe.greenminds.ecomind.community.domain.repositories.CommunityGoalRepository;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.CommunityGoalPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityGoalPersistenceRepository;

@Repository public class CommunityGoalRepositoryImpl implements CommunityGoalRepository{
    private final CommunityGoalPersistenceRepository repository;
    public CommunityGoalRepositoryImpl(CommunityGoalPersistenceRepository r){
        repository=r;
    }
    public CommunityGoal save(CommunityGoal g){
        var entity=g.id()==null?CommunityGoalPersistenceAssembler.toEntity(g):repository.findById(g.id()).orElseThrow();
        if(g.id()!=null)entity.applyProgress(g.progress(),g.participants());
        return CommunityGoalPersistenceAssembler.toDomain(repository.save(entity));
    }
    public Optional<CommunityGoal> findById(Long id){
        return repository.findById(id).map(CommunityGoalPersistenceAssembler::toDomain);
    }
    public List<CommunityGoal> findAll(Long c){
        return (c==null?repository.findAll():repository.findByCommunityId(c)).stream().map(CommunityGoalPersistenceAssembler::toDomain).toList();
    }
    public boolean existsActiveByCommunityId(Long c){
        return repository.existsByCommunityIdAndStatus(c, CommunityGoalStatus.ACTIVE);
    }
}
