package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate; import java.time.LocalTime;

@Schema(name="CreateEvent")
public record CreateEventResource(@NotNull Long community_id,@NotBlank String name,String description,@NotNull LocalDate date,@NotNull LocalTime start_time,String location,Double latitude,Double longitude,@NotNull @Positive Integer capacity,String image_url,@NotNull Long author_id){}
