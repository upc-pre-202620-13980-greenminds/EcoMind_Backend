package pe.greenminds.ecomind.community.domain.model.commands;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record CreateCommunityGoalCommand(Long communityId, CommunityGoalTopic topic, Integer target, Long requestedBy) {}
