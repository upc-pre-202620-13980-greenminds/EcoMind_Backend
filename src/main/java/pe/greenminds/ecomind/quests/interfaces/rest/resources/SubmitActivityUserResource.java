package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitActivityUserResource(
        @NotNull
        @Schema(
                description = "Submission data required by the activity type",
                example = "{\"checked\": true}"
        )
        Map<String, Object> data
) {
}
