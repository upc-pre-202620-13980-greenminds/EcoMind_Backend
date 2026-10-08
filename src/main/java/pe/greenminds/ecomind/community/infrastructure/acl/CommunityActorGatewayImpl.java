package pe.greenminds.ecomind.community.infrastructure.acl;

import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

@Component
public class CommunityActorGatewayImpl implements CommunityActorGateway {
    private final UsersContextFacade users;

    public CommunityActorGatewayImpl(UsersContextFacade users) {
        this.users = users;
    }

    public boolean isParent(Long userId) {
        return users.isParent(userId);
    }

    public void requireParent(Long userId) {
        if (!isParent(userId))
            throw new SecurityException("Only parent users may create communities or events");
    }

    public int requireFamilyParentAndCount(Long userId, Long familyId) {
        int count = users.getFamilyMemberIds(familyId).size();
        if (count == 0
                || !users.getFamilyRole(familyId, userId).filter("PARENT"::equals).isPresent())
            throw new SecurityException(
                    "Only a parent member of the selected family may register it");
        return count;
    }
}
