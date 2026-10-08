package pe.greenminds.ecomind.quests.application.internal.outboundservices.acl;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

/**
 * Anti-corruption layer used by Quests to query capabilities exposed by Users without depending
 * on its internal domain model.
 */
@Service
public class UsersServiceClient {

    private final UsersContextFacade usersContextFacade;

    public UsersServiceClient(UsersContextFacade usersContextFacade) {
        this.usersContextFacade = usersContextFacade;
    }

    public boolean existsUser(Long userId) {
        return usersContextFacade.existsUser(userId);
    }

    public boolean areFriends(Long userId, Long otherUserId) {
        return usersContextFacade.areFriends(userId, otherUserId);
    }

    public boolean belongToSameFamily(Long userId, Long otherUserId) {
        return usersContextFacade.getFamilyIdOfUser(userId)
                .map(familyId -> usersContextFacade.isFamilyMember(familyId, otherUserId))
                .orElse(false);
    }

    public boolean isFamilyMember(Long familyId, Long userId) {
        return usersContextFacade.isFamilyMember(familyId, userId);
    }

    public List<Long> getFamilyMemberIds(Long familyId) {
        return usersContextFacade.getFamilyMemberIds(familyId);
    }
}
