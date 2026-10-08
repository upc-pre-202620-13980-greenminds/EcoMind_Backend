package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunitiesQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.CommunityPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityPersistenceRepository;

@Repository
public class CommunityRepositoryImpl implements CommunityRepository {
    private final CommunityPersistenceRepository repository;

    public CommunityRepositoryImpl(CommunityPersistenceRepository persistenceRepository) {
        repository = persistenceRepository;
    }

    @Override
    public Community save(Community community) {
        return CommunityPersistenceAssembler.toDomain(repository.save(CommunityPersistenceAssembler.toEntity(community)));
    }

    @Override
    public Optional<Community> findById(Long id) {
        return repository.findById(id).map(CommunityPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public List<Community> search(SearchCommunitiesQuery query) {
        var list = query.type() == null
                ? (query.locality() == null ? repository.findAll()
                        : repository.findByTypeAndLocalityIgnoreCase(CommunityType.LOCAL, query.locality()))
                : query.type() == CommunityType.LOCAL && query.locality() != null
                        ? repository.findByTypeAndLocalityIgnoreCase(CommunityType.LOCAL, query.locality())
                        : repository.findByType(query.type());
        return list.stream().map(CommunityPersistenceAssembler::toDomain).toList();
    }
}
