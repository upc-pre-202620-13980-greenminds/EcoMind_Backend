package pe.greenminds.ecomind.community.interfaces.rest.resources;

import java.time.Instant;

public record EventParticipationResource(
        EventResource event,
        String community_name,
        String creator_name,
        int participant_count,
        int total_capacity,
        Long registration_id,
        boolean joined,
        boolean can_cancel_group,
        boolean creator,
        Instant chat_expires_at,
        boolean chat_available,
        Long family_id) {
}