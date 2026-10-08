package pe.greenminds.ecomind.quests.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.Date;

public record CollabQuestSessionResource(
        Long id,
        Long questId,
        Long ownerUserId,
        String status,
        Date createdAt,
        LocalDate startedAt,
        LocalDate completedAt
) {
}
