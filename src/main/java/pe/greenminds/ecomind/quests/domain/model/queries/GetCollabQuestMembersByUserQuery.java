package pe.greenminds.ecomind.quests.domain.model.queries;

import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;

public record GetCollabQuestMembersByUserQuery(Long userId, CollabMemberStatus status) {
}
