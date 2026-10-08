package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
import pe.greenminds.ecomind.community.domain.repositories.CommunityMembershipRepository;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers.CommunityMembershipPersistenceAssembler;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories.CommunityMembershipPersistenceRepository;

@Repository
public class CommunityMembershipRepositoryImpl implements CommunityMembershipRepository {
    private final CommunityMembershipPersistenceRepository repository;

    public CommunityMembershipRepositoryImpl(CommunityMembershipPersistenceRepository r) {
        repository = r;
    }

    public CommunityMembership save(CommunityMembership m) {
        return CommunityMembershipPersistenceAssembler
                .toDomain(repository.save(CommunityMembershipPersistenceAssembler.toEntity(m)));
    }

    public List<CommunityMembership> findByUserId(Long id) {
        return repository.findByUserId(id).stream().map(CommunityMembershipPersistenceAssembler::toDomain).toList();
    }

    public Optional<CommunityMembership> findByCommunityIdAndUserId(Long c, Long u) {
        return repository.findByCommunityIdAndUserId(c, u).map(CommunityMembershipPersistenceAssembler::toDomain);
    }

    public List<CommunityMembership> findByCommunityId(Long id) {
        return repository.findByCommunityId(id).stream().map(CommunityMembershipPersistenceAssembler::toDomain)
                .toList();
    }

    public long countByCommunityId(Long id) {
        return repository.countByCommunityId(id);
    }
}
