package pe.greenminds.ecomind.quests.application.queryservices;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
public record CollabQuestInvitation(Long memberId, Long sessionId, Long questId, String questTitle,
        Long organizerUserId, Long invitedUserId, CollabMemberStatus memberStatus, CollabQuestStatus sessionStatus) {}
