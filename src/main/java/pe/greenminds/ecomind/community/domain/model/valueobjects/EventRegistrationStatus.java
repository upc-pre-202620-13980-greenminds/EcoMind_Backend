package pe.greenminds.ecomind.community.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventRegistrationStatus {
    REGISTERED,
    CANCELLED;

    @JsonCreator
    public static EventRegistrationStatus fromValue(String value) {
        for (EventRegistrationStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported event registration status: " + value);
    }
}
