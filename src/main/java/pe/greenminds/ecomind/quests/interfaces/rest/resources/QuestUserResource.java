package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;

import java.time.LocalDate;

@Schema(
        name = "QuestUserResponse",
        description = "Quest assignment information",
        example = """
        {
          "id": 1,
          "userId": 1,
          "questId": 1,
          "status": "IN_PROGRESS",
          "progress": 0.0,
          "endDate": null,
          "collaborativeSessionId": null
        }
        """
)
public record QuestUserResource(
        Long id,
        Long userId,
        Long questId,
        QuestStatus status,
        Double progress,
        LocalDate endDate,
        Long collaborativeSessionId
) {
}
