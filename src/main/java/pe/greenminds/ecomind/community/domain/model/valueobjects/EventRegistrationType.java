package pe.greenminds.ecomind.community.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventRegistrationType {
    INDIVIDUAL,
    FAMILY;

    @JsonCreator
    public static EventRegistrationType fromValue(String value) {
        for (EventRegistrationType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported event registration type: " + value);
    }
}
