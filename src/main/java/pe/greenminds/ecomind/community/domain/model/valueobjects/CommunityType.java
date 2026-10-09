package pe.greenminds.ecomind.community.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommunityType {
    LOCAL("local"),
    TOPIC("topic");

    private final String value;

    CommunityType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static CommunityType fromValue(String value) {
        for (CommunityType type : values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported community type: " + value);
    }
}
