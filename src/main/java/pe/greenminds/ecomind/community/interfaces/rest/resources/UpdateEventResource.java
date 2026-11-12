package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "UpdateEvent")
public record UpdateEventResource(
        @Size(min = 1, max = 255)
        String name,

        @Size(max = 3000)
        String description,

        LocalDate date,

        LocalTime start_time,

        @Size(max = 255)
        String location,

        Double latitude,

        Double longitude,

        @Schema(description = "New total participant limit, including active registrations; omitted to keep the current limit")
        @Positive
        Integer capacity,

        @Size(max = 255)
        String image_url) {
}
