package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import java.time.LocalDate;

@Schema(
        name="QuestResponse",
        description = "Quest information response",
        example = """
        {
          "id": 1,
          "minigameId": null,
          "title": "Turn off unnecessary lights",
          "description": "Turn off lights that are not being used.",
          "category": "ENERGY",
          "type": "ACTIVITIES",
          "gemReward": 30,
          "ecopoints": 30,
          "age": 8,
          "time": 10,
          "theme": "CHECKBOX",
          "assignedDate": "2026-06-12",
          "image": "https://example.com/quest.png"
        }
        """
)
public record QuestResource (
    @Schema(description = "Quest unique identifier", example = "1")
    Long id,

    @Schema(description = "MinigameId for quest with minigame")
    Long minigameId,

    @Schema(description = "Quest title", example = "Turn off unnecessary lights", minLength = 1, maxLength = 100)
    String title,

    @Schema(description = "Quest description", example = "Check the rooms at home and turn off any lights you are not using. This small habit reduces energy use and helps protect the planet.", minLength = 1, maxLength = 255)
    String description,

    @Schema(description = "Quest category", example = "ENERGY", minLength = 1, maxLength = 50)
    Category category,

    @Schema(description = "Quest type", example = "ACTIVITIES", minLength = 1, maxLength = 50)
    QuestType type,

    @Schema(description = "Quest gem completition award", example = "30")
    Integer gemReward,

    @Schema(description = "Quest ecopoints completition award", example = "30")
    Integer ecopoints,

    @Schema(description = "Age that quest is directed towards", example = "0")
    Integer age,

    @Schema(description = "Expected time to complete the quest (in minutes)", example = "10")
    Integer time,

    @Schema(description = "Quest theme card", example = "CHECKBOX")
    Theme theme,

    @Schema(description = "Assigned date for daily quests", example="2026-06-12")
    LocalDate assignedDate,

    @Schema(description = "Image url for quest description", example="https://example.com/quest.png", minLength = 1, maxLength = 255)
    String image
){
}
