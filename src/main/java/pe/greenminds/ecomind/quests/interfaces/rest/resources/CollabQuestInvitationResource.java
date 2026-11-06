package pe.greenminds.ecomind.quests.interfaces.rest.resources;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;
public record CollabQuestInvitationResource(Long memberId, Long sessionId, Long questId, String questTitle,
        Long organizerUserId, Long invitedUserId, CollabMemberStatus memberStatus, CollabQuestStatus sessionStatus) {}
