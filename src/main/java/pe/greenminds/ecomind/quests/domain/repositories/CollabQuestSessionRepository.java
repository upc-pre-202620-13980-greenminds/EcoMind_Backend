package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;

import java.util.List;
import java.util.Optional;

public interface CollabQuestSessionRepository {
    CollabQuestSession save(CollabQuestSession collabQuestSession);
    Optional<CollabQuestSession> findById(Long id);

    Optional<CollabQuestSession> findByQuestIdAndOwnerUserId(Long questId, Long ownerUserId);

    Optional<CollabQuestSession> findByQuestIdAndOwnerUserIdAndStatusIn(
            Long questId,
            Long ownerUserId,
            List<CollabQuestStatus> statuses
    );

    List<CollabQuestSession> findByQuestId(Long questId);

    List<CollabQuestSession> findByOwnerUserId(Long ownerUserId);

    void deleteById(Long id);

    void deleteByQuestId(Long questId);
}
