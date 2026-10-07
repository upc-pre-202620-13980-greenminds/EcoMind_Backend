package pe.greenminds.ecomind.quests.application.queryservices;

import java.util.List;

public record QuestUserVersionStatus(
        Long questUserId,
        boolean upToDate,
        List<Long> missingActivityIds,
        List<Long> outdatedActivityIds
) {
}
