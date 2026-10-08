package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;

import java.util.List;
import java.util.Optional;

/** Uses only the supplier public contract, never its internal model. */
public interface CommunityServiceClient {
    Optional<Long> findLocalCommunity(Long userId);

    boolean isMember(Long communityId, Long userId);

    boolean mayPublishAchievement(Long communityId, Long userId);

    List<CommunityContextFacade.Member> findMembers(Long communityId);

    void recordAchievementNotice(CommunityContextFacade.AchievementNotice notice);

    void requestAchievementPublication(CommunityContextFacade.PublishAchievement command);
}
