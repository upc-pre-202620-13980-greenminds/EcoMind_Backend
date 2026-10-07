package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;

import java.time.LocalDateTime;

public record CollabQuestMemberResource(
        Long id,
        Long sessionId,
        Long userId,
        Long ownerId,
        MemberRole role,
        CollabMemberStatus status,
        LocalDateTime answerDate,
        LocalDateTime revokeDate
) {
}
