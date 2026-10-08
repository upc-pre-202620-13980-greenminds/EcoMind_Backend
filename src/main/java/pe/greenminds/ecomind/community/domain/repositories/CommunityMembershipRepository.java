package pe.greenminds.ecomind.community.domain.repositories;
import java.util.List;import java.util.Optional;import pe.greenminds.ecomind.community.domain.model.aggregates.CommunityMembership;
public interface CommunityMembershipRepository{CommunityMembership save(CommunityMembership membership);List<CommunityMembership> findByUserId(Long userId);Optional<CommunityMembership> findByCommunityIdAndUserId(Long communityId,Long userId);List<CommunityMembership> findByCommunityId(Long communityId);long countByCommunityId(Long communityId);}
