package pe.greenminds.ecomind.quests.domain.model.events;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;

import java.time.LocalDateTime;

public record CollabQuestMemberCreatedEvent(
        Long sessionId,
        Long userId,
        Long ownerId,
        MemberRole role,
        CollabMemberStatus status
) {
    public static CollabQuestMemberCreatedEvent from(CollabQuestMember collabQuestMember) {
        return new CollabQuestMemberCreatedEvent(
                collabQuestMember.getSessionId(),
                collabQuestMember.getUserId(),
                collabQuestMember.getOwnerId(),
                collabQuestMember.getRole(),
                collabQuestMember.getStatus()
        );
    }
}

