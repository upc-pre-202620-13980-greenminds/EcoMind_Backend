package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class FamilyPlanItem {
    @Getter
    @Setter
    private Long id;

    private Long familyPlanId;
    private Long questId;
    private Long collaborativeSessionId;

    public FamilyPlanItem(
            Long id,
            Long familyPlanId,
            Long questId,
            Long collaborativeSessionId
    ) {
        this.id = id;
        this.familyPlanId = Objects.requireNonNull(familyPlanId, "familyPlanId must not be null");
        this.questId = Objects.requireNonNull(questId, "questId must not be null");
        this.collaborativeSessionId = collaborativeSessionId;
    }

    public FamilyPlanItem(Long familyPlanId, Long questId) {
        this(null, familyPlanId, questId, null);
    }

    public void attachCollaborativeSession(Long collaborativeSessionId) {
        this.collaborativeSessionId = Objects.requireNonNull(
                collaborativeSessionId,
                "collaborativeSessionId must not be null"
        );
    }

    public Long getFamilyPlanId() { return familyPlanId; }
    public Long getQuestId() { return questId; }
    public Long getCollaborativeSessionId() { return collaborativeSessionId; }
}
