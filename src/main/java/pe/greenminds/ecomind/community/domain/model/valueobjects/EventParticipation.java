package pe.greenminds.ecomind.community.domain.model.valueobjects;

import java.time.Instant;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;

public record EventParticipation(
        Event event,
        String communityName,
        String creatorName,
        int participantCount,
        int totalCapacity,
        Long registrationId,
        boolean joined,
        boolean canCancelGroup,
        boolean creator,
        Instant chatExpiresAt,
        boolean chatAvailable,
        Long familyId) {
}