package pe.greenminds.ecomind.gamification.infrastructure.acl;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;

/** Resolves the real supplier when implemented; absence fails explicitly, never as an empty/success response. */
@Component
public class CommunityServiceClientImpl implements CommunityServiceClient {
  private final ObjectProvider<CommunityContextFacade> provider;
  public CommunityServiceClientImpl(ObjectProvider<CommunityContextFacade> provider) { this.provider = provider; }
  public Optional<UUID> findLocalCommunity(Long userId) { return requireSupplier().findLocalCommunity(userId); }
  public boolean isMember(UUID communityId, Long userId) { return requireSupplier().isMember(communityId, userId); }
  public boolean mayPublishAchievement(UUID communityId, Long userId) { return requireSupplier().mayPublishAchievement(communityId, userId); }
  public List<CommunityContextFacade.Member> findMembers(UUID communityId) { return requireSupplier().findMembers(communityId); }
  public void recordAchievementNotice(CommunityContextFacade.AchievementNotice notice) { requireSupplier().recordAchievementNotice(notice); }
  public void requestAchievementPublication(CommunityContextFacade.PublishAchievement command) { requireSupplier().requestAchievementPublication(command); }
  private CommunityContextFacade requireSupplier() {
    var supplier = provider.getIfAvailable();
    if (supplier == null) throw new IllegalStateException("Community integration is not implemented yet");
    return supplier;
  }
}
