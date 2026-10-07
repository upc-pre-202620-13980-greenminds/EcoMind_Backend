package pe.greenminds.ecomind.quests.domain.repositories;

import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;

import java.util.List;

public interface CollabQuestMemberRepository {
    CollabQuestMember save(CollabQuestMember collabQuestMember);
    boolean existsById(Long id);
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
    CollabQuestMember findById(Long id);
    CollabQuestMember findBySessionIdAndUserId(Long sessionId, Long userId);
    void deleteBySessionId(Long sessionId);
    List<CollabQuestMember> findBySessionIdAndStatusIn(
            Long sessionId,
            List<CollabMemberStatus> statuses
    );
    List<CollabQuestMember> findByUserIdAndQuestId(Long userId, Long questId);
    List<CollabQuestMember> findByUserIdAndQuestIdAndSessionStatusIn(
            Long userId,
            Long questId,
            List<CollabQuestStatus> sessionStatuses
    );
}
