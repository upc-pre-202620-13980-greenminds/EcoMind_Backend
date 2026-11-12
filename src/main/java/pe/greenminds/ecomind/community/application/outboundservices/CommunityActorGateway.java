package pe.greenminds.ecomind.community.application.outboundservices;

import java.util.Set;

public interface CommunityActorGateway {
    boolean isParent(Long userId);

    void requireParent(Long userId);

    int requireFamilyParentAndCount(Long userId, Long familyId);

    Set<Long> requireFamilyParentAndGetMembers(Long userId, Long familyId);
}
