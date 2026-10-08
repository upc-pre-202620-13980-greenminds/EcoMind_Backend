package pe.greenminds.ecomind.community.domain.model.aggregates;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

public record CommunityGoal(Long id, Long communityId, CommunityGoalTopic topic, Integer target,
                            Integer progress, Integer participants, String status) {
    public CommunityGoal{
        if (communityId == null || topic == null || target == null || target < 1 || progress == null
                || progress < 0 || progress > target || participants == null || participants < 0)
            throw new IllegalArgumentException("Community goal fields are invalid");
        if (!"active".equals(status) && !"completed".equals(status))
            throw new IllegalArgumentException("Community goal status is invalid");
    }

    public String title() {
        return "Complete " + target + " " + topic.value() + " quests";
    }
}
