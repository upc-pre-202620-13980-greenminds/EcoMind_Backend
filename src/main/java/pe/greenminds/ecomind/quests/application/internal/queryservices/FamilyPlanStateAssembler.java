package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanItemState;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanState;
import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlan;
import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;

@Component
public class FamilyPlanStateAssembler {
    private final FamilyPlanItemRepository familyPlanItemRepository;
    private final QuestUserRepository questUserRepository;

    public FamilyPlanStateAssembler(
            FamilyPlanItemRepository familyPlanItemRepository,
            QuestUserRepository questUserRepository
    ) {
        this.familyPlanItemRepository = familyPlanItemRepository;
        this.questUserRepository = questUserRepository;
    }

    public FamilyPlanState toState(FamilyPlan familyPlan) {
        var itemStates = familyPlanItemRepository.findByFamilyPlanId(familyPlan.getId())
                .stream()
                .map(this::toItemState)
                .toList();
        var progress = itemStates.stream()
                .map(FamilyPlanItemState::progress)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return new FamilyPlanState(
                familyPlan.getId(),
                familyPlan.getFamilyId(),
                familyPlan.getOwnerUserId(),
                familyPlan.getStatus(),
                progress,
                itemStates
        );
    }

    private FamilyPlanItemState toItemState(FamilyPlanItem item) {
        return new FamilyPlanItemState(
                item.getId(),
                item.getQuestId(),
                item.getCollaborativeSessionId(),
                calculateProgress(item)
        );
    }

    private Double calculateProgress(FamilyPlanItem item) {
        if (item.getCollaborativeSessionId() == null) {
            return 0.0;
        }

        return questUserRepository.findByQuestId(item.getQuestId())
                .stream()
                .filter(questUser -> item.getCollaborativeSessionId().equals(
                        questUser.getCollaborativeSessionId()
                ))
                .map(questUser -> questUser.getProgress())
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
