package pe.greenminds.ecomind.community.domain.model.commands;

public record JoinCommunityGoalCommand(Long goalId, Long requestedBy) {
}