package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.queryservices.CommunityQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.queries.*;
import pe.greenminds.ecomind.community.domain.repositories.*;

@Service
public class CommunityQueryServiceImpl implements CommunityQueryService {
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;

    public CommunityQueryServiceImpl(CommunityRepository communityRepository,
            CommunityMembershipRepository communityMembershipRepository) {
        communities = communityRepository;
        memberships = communityMembershipRepository;
    }

    @Override
    public List<Community> handle(SearchCommunitiesQuery query) {
        return communities.search(query);
    }

    @Override
    public List<CommunityMembership> handle(GetCommunityMembershipsByUserQuery query) {
        return memberships.findByUserId(query.userId());
    }

    @Override
    public List<CommunityMembership> handle(GetCommunityMembershipsByCommunityQuery query) {
        return memberships.findByCommunityId(query.communityId());
    }
}
