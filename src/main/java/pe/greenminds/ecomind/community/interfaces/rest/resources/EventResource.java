package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventResource(Long id, Long community_id, Long author_id, String name, String description, LocalDate date,
        LocalTime start_time, String location, Double latitude, Double longitude,
        @Schema(description = "Available participant places remaining; zero means the event is full") Integer capacity,
        String image_url) {
}
