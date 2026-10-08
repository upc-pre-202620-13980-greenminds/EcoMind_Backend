package pe.greenminds.ecomind.quests.application.internal.eventhandlers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class IntegrationEventId {
    private IntegrationEventId() {
    }

    public static UUID forCompletion(String completionType, Long aggregateId) {
        return UUID.nameUUIDFromBytes(
                (completionType + ":" + aggregateId).getBytes(StandardCharsets.UTF_8)
        );
    }
}
