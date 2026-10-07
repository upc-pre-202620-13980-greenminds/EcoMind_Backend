package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities.CollabQuestMemberPersistenceEntity;

public final class CollabQuestMemberPersistenceAssembler {
    private CollabQuestMemberPersistenceAssembler() {
    }

    public static CollabQuestMember toDomainFromPersistence(
            CollabQuestMemberPersistenceEntity entity
    ) {
        return new CollabQuestMember(
                entity.getId(),
                entity.getSessionId(),
                entity.getUserId(),
                entity.getOwnerId(),
                entity.getRole(),
                entity.getStatus(),
                entity.getAnswerDate(),
                entity.getRevokeDate()
        );
    }

    public static CollabQuestMemberPersistenceEntity toPersistenceFromDomain(
            CollabQuestMember collabQuestMember
    ) {
        var entity = new CollabQuestMemberPersistenceEntity();
        entity.setId(collabQuestMember.getId());
        entity.setSessionId(collabQuestMember.getSessionId());
        entity.setUserId(collabQuestMember.getUserId());
        entity.setOwnerId(collabQuestMember.getOwnerId());
        entity.setRole(collabQuestMember.getRole());
        entity.setStatus(collabQuestMember.getStatus());
        entity.setAnswerDate(collabQuestMember.getAnswerDate());
        entity.setRevokeDate(collabQuestMember.getRevokeDate());
        return entity;
    }
}
