package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import java.time.LocalDate;

@Schema(name = "UpdateQuestRequest", description = "Complete quest update payload")
public record UpdateQuestResource(
        Long minigameId,

        @NotBlank(message = "is required")
        String title,

        @NotNull(message = "is required")
        String description,

        @NotNull(message = "is required")
        Category category,

        @NotNull(message = "is required")
        QuestType type,

        @NotNull
        @PositiveOrZero
        Integer gemReward,

        @NotNull
        @PositiveOrZero
        Integer ecopoints,

        Integer age,
        Integer time,

        @NotNull(message = "is required")
        Theme theme,

        LocalDate assignedDate,
        String image
) {
}
