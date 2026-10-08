package pe.greenminds.ecomind.community.interfaces.rest.resources;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record CommunityGoalResource(Long id, Long community_id, CommunityGoalTopic topic, String title,
                                    Integer target, Integer progress, Integer participants, String status) {}
