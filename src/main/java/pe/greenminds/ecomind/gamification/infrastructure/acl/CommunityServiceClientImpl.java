package pe.greenminds.ecomind.gamification.infrastructure.acl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;
import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationDependencyUnavailableException;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the real supplier when implemented; absence fails explicitly, never as an empty/success
 * response.
 */
@Component
public class CommunityServiceClientImpl implements CommunityServiceClient {
    private final ObjectProvider<CommunityContextFacade> provider;

    public CommunityServiceClientImpl(ObjectProvider<CommunityContextFacade> provider) {
        this.provider = provider;
    }

    public Optional<Long> findLocalCommunity(Long userId) {
        return requireSupplier().findLocalCommunity(userId);
    }

    public boolean isMember(Long communityId, Long userId) {
        return requireSupplier().isMember(communityId, userId);
    }

    public boolean mayPublishAchievement(Long communityId, Long userId) {
        return requireSupplier().mayPublishAchievement(communityId, userId);
    }

    public List<CommunityContextFacade.Member> findMembers(Long communityId) {
        return requireSupplier().findMembers(communityId);
    }

    public void recordAchievementNotice(CommunityContextFacade.AchievementNotice notice) {
        requireSupplier().recordAchievementNotice(notice);
    }

    public void requestAchievementPublication(CommunityContextFacade.PublishAchievement command) {
        requireSupplier().requestAchievementPublication(command);
    }

    private CommunityContextFacade requireSupplier() {
        var supplier = provider.getIfAvailable();
        if (supplier == null)
            throw new GamificationDependencyUnavailableException(
                    "Community integration is not implemented yet");
        return supplier;
    }
}
