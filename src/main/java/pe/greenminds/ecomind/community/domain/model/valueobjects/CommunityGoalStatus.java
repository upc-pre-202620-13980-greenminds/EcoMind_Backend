package pe.greenminds.ecomind.community.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommunityGoalStatus {
    ACTIVE,
    COMPLETED;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static CommunityGoalStatus fromValue(String value) {
        for (CommunityGoalStatus status : values()) {
            if (status.value().equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported community goal status: " + value);
    }
}
