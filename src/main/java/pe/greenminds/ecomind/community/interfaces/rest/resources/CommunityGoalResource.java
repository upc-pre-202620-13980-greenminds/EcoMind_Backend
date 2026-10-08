package pe.greenminds.ecomind.community.interfaces.rest.resources;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;

public record CommunityGoalResource(Long id, Long community_id, CommunityGoalTopic topic, String title,
                                    Integer target, Integer progress, Integer participants, CommunityGoalStatus status) {}
