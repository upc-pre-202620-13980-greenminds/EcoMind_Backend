package pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.interfaces.acl.CommunityContextFacade;

/** Reads the members of a community, who are the audience of the community notifications. */
// Own bean name: Gamification already has a CommunityServiceClient
@Service("notificationsCommunityServiceClient")
public class CommunityServiceClient {

  private final CommunityContextFacade communityContextFacade;

  public CommunityServiceClient(CommunityContextFacade communityContextFacade) {
    this.communityContextFacade = communityContextFacade;
  }

  public List<Long> findMemberIds(Long communityId) {
    return communityContextFacade.findMembers(communityId).stream()
        .map(CommunityContextFacade.Member::userId)
        .toList();
  }
}
