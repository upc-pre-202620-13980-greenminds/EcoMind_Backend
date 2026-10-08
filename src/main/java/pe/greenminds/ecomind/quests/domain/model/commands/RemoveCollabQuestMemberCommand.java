package pe.greenminds.ecomind.quests.domain.model.commands;

public record RemoveCollabQuestMemberCommand(Long memberId, Long ownerUserId) {
}
