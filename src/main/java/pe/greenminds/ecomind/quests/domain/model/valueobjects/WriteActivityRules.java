package pe.greenminds.ecomind.quests.domain.model.valueobjects;

import java.util.Map;

public final class WriteActivityRules {
    private static final String MIN_CHARACTERS = "minCharacters";

    private WriteActivityRules() {
    }

    public static int requireMinCharacters(Map<String, Object> configuration) {
        var value = configuration == null ? null : configuration.get(MIN_CHARACTERS);
        if (!(value instanceof Integer minimum) || minimum < 1) {
            throw new IllegalArgumentException("activityConfiguration.minCharacters must be a positive integer");
        }
        return minimum;
    }

    public static int minCharactersForAssignment(Map<String, Object> configuration) {
        // Older assignments accepted any nonblank response.
        if (configuration == null || !configuration.containsKey(MIN_CHARACTERS)) {
            return 1;
        }
        return requireMinCharacters(configuration);
    }
}
