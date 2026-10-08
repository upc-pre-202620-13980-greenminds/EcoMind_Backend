package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "CreateEvent")
public record CreateEventResource(
        @Schema(description = "Community identifier", example = "1") @NotNull Long community_id,
        @Schema(description = "Event name", example = "Community garden cleanup") @NotBlank String name,
        @Schema(description = "Event details", example = "We will clean and prepare the neighborhood garden.")
        String description,
        @Schema(description = "Event date", example = "2026-11-09") @NotNull LocalDate date,
        @Schema(description = "Event start time", example = "12:00:00") @NotNull LocalTime start_time,
        @Schema(description = "Event address or location", example = "315 Paloma Avenue, Lima") String location,
        @Schema(description = "Latitude of the event location", example = "-12.0464") Double latitude,
        @Schema(description = "Longitude of the event location", example = "-77.0428") Double longitude,
        @Schema(description = "Maximum number of participants", example = "40")
        @NotNull @Positive Integer capacity,
        @Schema(description = "Event image URL", example = "https://example.com/garden-cleanup.png") String image_url) {
}
