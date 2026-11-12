package pe.greenminds.ecomind.community.domain.model.commands;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateEventCommand(
        Long eventId,
        Long requestedBy,
        String name,
        String description,
        LocalDate date,
        LocalTime startTime,
        String location,
        Double latitude,
        Double longitude,
        Integer capacity,
        String imageUrl) {
}