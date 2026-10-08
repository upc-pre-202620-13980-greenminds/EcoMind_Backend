package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;

public final class CommunityPersistenceAssembler {
    private CommunityPersistenceAssembler() {
    }

    public static Community toDomain(CommunityPersistenceEntity persistenceEntity) {
        return new Community(persistenceEntity.getId(), persistenceEntity.getName(), persistenceEntity.getDescription(),
                persistenceEntity.getType(), persistenceEntity.getTopic(), persistenceEntity.getLocality(),
                persistenceEntity.getMemberLimit(), persistenceEntity.getIconUrl(), persistenceEntity.getCreatedBy());
    }

    public static CommunityPersistenceEntity toEntity(Community community) {
        return new CommunityPersistenceEntity(community.getId(), community.getName(), community.getDescription(),
                community.getType(), community.getTopic(), community.getLocality(), community.getMemberLimit(),
                community.getIconUrl(), community.getCreatedBy());
    }
}
