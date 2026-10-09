package pe.greenminds.ecomind.community.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommunityGoalTopic {
    WATER("water"),
    ENERGY("energy"),
    RECYCLE("recycle");

    private final String value;

    CommunityGoalTopic(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static CommunityGoalTopic fromValue(String value) {
        for (CommunityGoalTopic topic : values()) {
            if (topic.value.equalsIgnoreCase(value) || topic.name().equalsIgnoreCase(value)) {
                return topic;
            }
        }
        throw new IllegalArgumentException("Unsupported community goal topic: " + value);
    }
}
