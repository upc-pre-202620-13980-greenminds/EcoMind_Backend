package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.greenminds.ecomind.community.domain.model.commands.UpdateEventCommand;

public class Event {
    private final Long id, communityId, authorId;
    private final String name, description, location, imageUrl;
    private final LocalDate date;
    private final LocalTime startTime;
    private final Double latitude, longitude;
    private final Integer capacity;

    public Event(Long id, Long communityId, Long authorId, String name, String description, LocalDate date,
            LocalTime startTime, String location, Double latitude, Double longitude, Integer capacity,
            String imageUrl) {
        if (communityId == null || authorId == null || name == null || name.isBlank() || date == null
                || startTime == null || capacity == null || capacity < 0)
            throw new IllegalArgumentException("Event fields are invalid");
        this.id = id;
        this.communityId = communityId;
        this.authorId = authorId;
        this.name = name.trim();
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.imageUrl = imageUrl;
    }

    public Event reservePlaces(int count) {
        if (count < 1 || count > capacity)
            throw new IllegalArgumentException("Insufficient available event places");
        return withCapacity(capacity - count);
    }

    public Event releasePlaces(int count) {
        if (count < 1)
            throw new IllegalArgumentException("Participant count must be positive");
        return withCapacity(Math.addExact(capacity, count));
    }

    private Event withCapacity(int availablePlaces) {
        return new Event(id, communityId, authorId, name, description, date, startTime,
                location, latitude, longitude, availablePlaces, imageUrl);
    }

    public Event updateDetails(UpdateEventCommand command) {
        if ((command.latitude() == null) != (command.longitude() == null)) {
            throw new IllegalArgumentException(
                    "Latitude and longitude must be provided together");
        }

        if (command.latitude() != null) {
            if (!Double.isFinite(command.latitude())
                    || command.latitude() < -90
                    || command.latitude() > 90) {
                throw new IllegalArgumentException(
                        "Latitude must be between -90 and 90");
            }

            if (!Double.isFinite(command.longitude())
                    || command.longitude() < -180
                    || command.longitude() > 180) {
                throw new IllegalArgumentException(
                        "Longitude must be between -180 and 180");
            }
        }

        return new Event(
                id,
                communityId,
                authorId,
                command.name() != null ? command.name() : name,
                updatedOptionalText(description, command.description()),
                command.date() != null ? command.date() : date,
                command.startTime() != null ? command.startTime() : startTime,
                updatedOptionalText(location, command.location()),
                command.latitude() != null ? command.latitude() : latitude,
                command.longitude() != null ? command.longitude() : longitude,
                command.capacity() != null ? command.capacity() : capacity,
                updatedOptionalText(imageUrl, command.imageUrl()));
    }

    private static String updatedOptionalText(
            String currentValue,
            String requestedValue) {

        if (requestedValue == null) {
            return currentValue;
        }

        String trimmed = requestedValue.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() {
        return id;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public String getLocation() {
        return location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
