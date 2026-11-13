package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

public record ActivityUserResource(
        Long id,
        Long questUserId,
        Long activityId,
        Double progress,
        LocalDate endDate,
        String activityDescription,
        Map<String, Object> activityConfiguration,
        Long collaborativeSessionId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Saved text for a submitted WRITE activity", example = "Apagaré la luz al salir.")
        String responseText
) {
}
