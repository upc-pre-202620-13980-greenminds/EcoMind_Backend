package pe.greenminds.ecomind.community.infrastructure.acl;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;
import java.util.Set;

@Component
public class CommunityActorGatewayImpl implements CommunityActorGateway {
    private final UsersContextFacade users;

    public CommunityActorGatewayImpl(UsersContextFacade users) {
        this.users = users;
    }

    @Override
    public boolean isParent(Long userId) {
        return users.isParent(userId);
    }

    @Override
    public void requireParent(Long userId) {
        if (!isParent(userId)) {
            throw new SecurityException("Only parent users may create communities or events");
        }
    }

    @Override
    public int requireFamilyParentAndCount(Long userId, Long familyId) {
        return requireFamilyParentAndGetMembers(userId, familyId).size();
    }

    @Override
    public Set<Long> requireFamilyParentAndGetMembers(Long userId, Long familyId) {
        var participants = Set.copyOf(users.getFamilyMemberIds(familyId));

        if (participants.isEmpty()
                || !participants.contains(userId)
                || users.getFamilyRole(familyId, userId).filter("PARENT"::equals).isEmpty()) {
            throw new SecurityException("Only a parent member of the selected family may register it");
        }

        return participants;
    }

    @Override
    public String getUserName(Long userId) {
        return users.getUserName(userId).orElse("User " + userId);
    }

    @Override
    public Long getRegistrableFamilyId(Long userId) {
        if (!users.isParent(userId)) {
            return null;
        }

        return users.getFamilyIdOfUser(userId)
                .filter(familyId -> users.getFamilyRole(familyId, userId)
                        .filter("PARENT"::equals)
                        .isPresent())
                .orElse(null);
    }
}
