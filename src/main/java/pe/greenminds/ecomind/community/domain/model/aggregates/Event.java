package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.time.LocalDate;
import java.time.LocalTime;

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
                || startTime == null || capacity == null || capacity < 1)
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
