package pe.greenminds.ecomind.quests.domain.model.commands;

public record DeletePendingCollabQuestSessionCommand(Long sessionId, Long ownerUserId) {
}
