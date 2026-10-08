package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Uses only the supplier public contract, never its internal model. */
public interface CommunityServiceClient {
    Optional<UUID> findLocalCommunity(Long userId);

    boolean isMember(UUID communityId, Long userId);

    boolean mayPublishAchievement(UUID communityId, Long userId);

    List<CommunityContextFacade.Member> findMembers(UUID communityId);

    void recordAchievementNotice(CommunityContextFacade.AchievementNotice notice);

    void requestAchievementPublication(CommunityContextFacade.PublishAchievement command);
}
