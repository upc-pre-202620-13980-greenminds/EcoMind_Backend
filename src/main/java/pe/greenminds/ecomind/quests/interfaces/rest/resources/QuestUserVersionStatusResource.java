package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "QuestUserVersionStatusResponse",
        description = "Quest assignment version status"
)
public record QuestUserVersionStatusResource(
        Long questUserId,
        boolean upToDate,
        List<Long> missingActivityIds,
        List<Long> outdatedActivityIds
) {
}
