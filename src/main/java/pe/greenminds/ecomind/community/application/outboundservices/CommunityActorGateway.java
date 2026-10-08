package pe.greenminds.ecomind.community.application.outboundservices;

public interface CommunityActorGateway {
    boolean isParent(Long userId);

    void requireParent(Long userId);

    int requireFamilyParentAndCount(Long userId, Long familyId);
}
