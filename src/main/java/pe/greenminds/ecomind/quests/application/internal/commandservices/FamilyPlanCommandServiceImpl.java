package pe.greenminds.ecomind.quests.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.commandservices.FamilyPlanCommandService;
import pe.greenminds.ecomind.quests.application.internal.queryservices.FamilyPlanStateAssembler;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanState;
import pe.greenminds.ecomind.quests.domain.model.aggregates.ActivityUser;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestSession;
import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlan;
import pe.greenminds.ecomind.quests.domain.model.aggregates.FamilyPlanItem;
import pe.greenminds.ecomind.quests.domain.model.aggregates.QuestUser;
import pe.greenminds.ecomind.quests.domain.model.commands.ActivateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CompleteFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeleteFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.FamilyPlanItemCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.UpdateFamilyPlanCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.MemberRole;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityUserRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanItemRepository;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestUserRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

import java.util.List;

@Service
public class FamilyPlanCommandServiceImpl implements FamilyPlanCommandService {
    private static final List<QuestStatus> ACTIVE_QUEST_USER_STATUSES =
            List.of(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE);

    private final FamilyPlanRepository familyPlanRepository;
    private final FamilyPlanItemRepository familyPlanItemRepository;
    private final QuestRepository questRepository;
    private final ActivityRepository activityRepository;
    private final CollabQuestSessionRepository collabQuestSessionRepository;
    private final CollabQuestMemberRepository collabQuestMemberRepository;
    private final QuestUserRepository questUserRepository;
    private final ActivityUserRepository activityUserRepository;
    private final FamilyPlanStateAssembler familyPlanStateAssembler;

    public FamilyPlanCommandServiceImpl(
            FamilyPlanRepository familyPlanRepository,
            FamilyPlanItemRepository familyPlanItemRepository,
            QuestRepository questRepository,
            ActivityRepository activityRepository,
            CollabQuestSessionRepository collabQuestSessionRepository,
            CollabQuestMemberRepository collabQuestMemberRepository,
            QuestUserRepository questUserRepository,
            ActivityUserRepository activityUserRepository,
            FamilyPlanStateAssembler familyPlanStateAssembler
    ) {
        this.familyPlanRepository = familyPlanRepository;
        this.familyPlanItemRepository = familyPlanItemRepository;
        this.questRepository = questRepository;
        this.activityRepository = activityRepository;
        this.collabQuestSessionRepository = collabQuestSessionRepository;
        this.collabQuestMemberRepository = collabQuestMemberRepository;
        this.questUserRepository = questUserRepository;
        this.activityUserRepository = activityUserRepository;
        this.familyPlanStateAssembler = familyPlanStateAssembler;
    }

    @Transactional
    @Override
    public Result<FamilyPlanState, ApplicationError> handle(CreateFamilyPlanCommand command) {
        var validation = validateFamilyPlanInput(
                command.familyId(),
                command.ownerUserId(),
                command.items()
        );
        if (validation != null) {
            return Result.failure(validation);
        }

        var plan = familyPlanRepository.save(
                new FamilyPlan(command.familyId(), command.ownerUserId())
        );
        command.items().forEach(item -> familyPlanItemRepository.save(
                new FamilyPlanItem(plan.getId(), item.questId())
        ));

        return Result.success(familyPlanStateAssembler.toState(plan));
    }

    @Transactional
    @Override
    public Result<FamilyPlanState, ApplicationError> handle(UpdateFamilyPlanCommand command) {
        var plan = familyPlanRepository.findById(command.familyPlanId());
        if (plan.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "FamilyPlan",
                    command.familyPlanId().toString()
            ));
        }
        if (plan.get().getStatus() != FamilyPlanStatus.DRAFT) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Only draft family plans can be edited",
                    "FamilyPlan %d is %s".formatted(command.familyPlanId(), plan.get().getStatus())
            ));
        }

        var validation = validateFamilyPlanInput(
                plan.get().getFamilyId(),
                plan.get().getOwnerUserId(),
                command.items()
        );
        if (validation != null) {
            return Result.failure(validation);
        }

        familyPlanItemRepository.deleteByFamilyPlanId(plan.get().getId());
        command.items().forEach(item -> familyPlanItemRepository.save(
                new FamilyPlanItem(plan.get().getId(), item.questId())
        ));

        return Result.success(familyPlanStateAssembler.toState(plan.get()));
    }

    @Transactional
    @Override
    public Result<FamilyPlanState, ApplicationError> handle(ActivateFamilyPlanCommand command) {
        var plan = familyPlanRepository.findById(command.familyPlanId());
        if (plan.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "FamilyPlan",
                    command.familyPlanId().toString()
            ));
        }
        if (plan.get().getStatus() != FamilyPlanStatus.DRAFT) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Only draft family plans can be activated",
                    "FamilyPlan %d is %s".formatted(command.familyPlanId(), plan.get().getStatus())
            ));
        }

        var items = familyPlanItemRepository.findByFamilyPlanId(command.familyPlanId());
        if (items.isEmpty()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Family plan must have at least one item",
                    "FamilyPlan %d has no items".formatted(command.familyPlanId())
            ));
        }

        // Membership validation belongs to the future users/profile integration.
        // Until that boundary exists, the owner is the only known participant.
        var familyMembers = List.of(plan.get().getOwnerUserId());

        for (var item : items) {
            var validation = validateFamilyQuest(item.getQuestId());
            if (validation != null) {
                return Result.failure(validation);
            }
            for (var familyMember : familyMembers) {
                if (questUserRepository.existsByUserIdAndQuestIdAndStatusIn(
                        familyMember,
                        item.getQuestId(),
                        ACTIVE_QUEST_USER_STATUSES
                )) {
                    if (!hasBlockingActiveQuest(familyMember, item.getQuestId())) {
                        continue;
                    }
                    return Result.failure(ApplicationError.conflict(
                            "QuestUser",
                            "User %d already has quest %d active".formatted(
                                    familyMember,
                                    item.getQuestId()
                            )
                    ));
                }
            }
        }

        for (var item : items) {
            var session = collabQuestSessionRepository.save(
                    new CollabQuestSession(
                            item.getQuestId(),
                            plan.get().getOwnerUserId(),
                            CollabQuestStatus.STARTED,
                            java.time.LocalDate.now(),
                            null
                    )
            );

            for (var familyMember : familyMembers) {
                var role = familyMember.equals(plan.get().getOwnerUserId())
                        ? MemberRole.OWNER
                        : MemberRole.PARTICIPANT;
                collabQuestMemberRepository.save(
                        new CollabQuestMember(
                                session.getId(),
                                familyMember,
                                plan.get().getOwnerUserId(),
                                role,
                                CollabMemberStatus.ACCEPTED
                        )
                );

                var questUser = questUserRepository.save(
                        new QuestUser(familyMember, item.getQuestId(), session.getId())
                );
                activityRepository.findByQuestsIdOrderByOrderAsc(item.getQuestId())
                        .forEach(activity -> activityUserRepository.save(
                                new ActivityUser(
                                        questUser.getId(),
                                        activity.getId(),
                                        activity.getDescription(),
                                        activity.getActivityConfiguration(),
                                        session.getId()
                                )
                        ));
            }

            item.attachCollaborativeSession(session.getId());
            familyPlanItemRepository.save(item);
        }

        plan.get().activate();
        var savedPlan = familyPlanRepository.save(plan.get());
        return Result.success(familyPlanStateAssembler.toState(savedPlan));
    }

    @Transactional
    @Override
    public Result<FamilyPlanState, ApplicationError> handle(CompleteFamilyPlanCommand command) {
        var plan = familyPlanRepository.findById(command.familyPlanId());
        if (plan.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "FamilyPlan",
                    command.familyPlanId().toString()
            ));
        }
        if (plan.get().getStatus() != FamilyPlanStatus.ACTIVE) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Only active family plans can be completed",
                    "FamilyPlan %d is %s".formatted(command.familyPlanId(), plan.get().getStatus())
            ));
        }
        if (!plan.get().getOwnerUserId().equals(command.ownerUserId())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Only the family plan owner can complete the plan",
                    "User %d is not owner of FamilyPlan %d".formatted(
                            command.ownerUserId(),
                            command.familyPlanId()
                    )
            ));
        }

        var items = familyPlanItemRepository.findByFamilyPlanId(command.familyPlanId());
        if (items.isEmpty()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Family plan must have at least one item",
                    "FamilyPlan %d has no items".formatted(command.familyPlanId())
            ));
        }

        for (var item : items) {
            var result = completeFamilyPlanItem(item);
            if (result != null) {
                return Result.failure(result);
            }
        }

        plan.get().complete();
        var savedPlan = familyPlanRepository.save(plan.get());
        return Result.success(familyPlanStateAssembler.toState(savedPlan));
    }

    @Transactional
    @Override
    public Result<FamilyPlanState, ApplicationError> handle(DeleteFamilyPlanCommand command) {
        var plan = familyPlanRepository.findById(command.familyPlanId());
        if (plan.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "FamilyPlan",
                    command.familyPlanId().toString()
            ));
        }

        if (plan.get().getStatus() == FamilyPlanStatus.DRAFT) {
            familyPlanItemRepository.deleteByFamilyPlanId(plan.get().getId());
            familyPlanRepository.deleteById(plan.get().getId());
            return Result.success(familyPlanStateAssembler.toState(plan.get()));
        }

        if (plan.get().getStatus() != FamilyPlanStatus.ACTIVE) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Only draft or active family plans can be deleted",
                    "FamilyPlan %d is %s".formatted(command.familyPlanId(), plan.get().getStatus())
            ));
        }

        familyPlanItemRepository.findByFamilyPlanId(plan.get().getId())
                .forEach(item -> {
                    cleanupFamilyPlanItemProgress(item);
                    if (item.getCollaborativeSessionId() == null) {
                        return;
                    }
                    collabQuestSessionRepository.findById(item.getCollaborativeSessionId())
                            .filter(session -> session.getStatus() == CollabQuestStatus.STARTED)
                            .ifPresent(session -> {
                                session.cancel();
                                collabQuestSessionRepository.save(session);
                            });
                });

        plan.get().cancel();
        var savedPlan = familyPlanRepository.save(plan.get());
        return Result.success(familyPlanStateAssembler.toState(savedPlan));
    }

    private ApplicationError validateFamilyPlanInput(
            Long familyId,
            Long ownerUserId,
            List<FamilyPlanItemCommand> items
    ) {
        var requestedItems = items == null ? List.<FamilyPlanItemCommand>of() : items;
        for (var item : requestedItems) {
            var validation = validateFamilyQuest(item.questId());
            if (validation != null) {
                return validation;
            }
        }
        return null;
    }

    private ApplicationError validateFamilyQuest(Long questId) {
        var quest = questRepository.findById(questId);
        if (quest.isEmpty()) {
            return ApplicationError.notFound("Quest", questId.toString());
        }
        if (quest.get().getType() != QuestType.FAMILY) {
            return ApplicationError.businessRuleViolation(
                    "Family plans only accept family quests",
                    "Quest %d is %s".formatted(questId, quest.get().getType())
            );
        }
        if (activityRepository.countByQuestId(questId) < 1) {
            return ApplicationError.businessRuleViolation(
                    "Family quest must have at least one activity",
                    "Quest %d has no activities".formatted(questId)
            );
        }
        return null;
    }

    private ApplicationError completeFamilyPlanItem(FamilyPlanItem item) {
        if (item.getCollaborativeSessionId() == null) {
            return ApplicationError.businessRuleViolation(
                    "Family plan item has no collaborative session",
                    "FamilyPlanItem %d is not activated".formatted(item.getId())
            );
        }

        var session = collabQuestSessionRepository.findById(item.getCollaborativeSessionId());
        if (session.isEmpty()) {
            return ApplicationError.notFound(
                    "CollabQuestSession",
                    item.getCollaborativeSessionId().toString()
            );
        }

        if (session.get().getStatus() == CollabQuestStatus.COMPLETED) {
            return null;
        }
        if (session.get().getStatus() != CollabQuestStatus.STARTED) {
            return ApplicationError.businessRuleViolation(
                    "Family plan item session must be started or completed",
                    "Session %d is %s".formatted(session.get().getId(), session.get().getStatus())
            );
        }

        var questUsers = questUserRepository.findByQuestId(item.getQuestId())
                .stream()
                .filter(questUser -> session.get().getId().equals(
                        questUser.getCollaborativeSessionId()
                ))
                .toList();
        if (questUsers.isEmpty()) {
            return ApplicationError.notFound(
                    "QuestUser",
                    "questId=%d, collaborativeSessionId=%d".formatted(
                            item.getQuestId(),
                            session.get().getId()
                    )
            );
        }

        var notReady = questUsers.stream()
                .filter(questUser -> questUser.getStatus() != QuestStatus.READY_TO_COMPLETE
                        && questUser.getStatus() != QuestStatus.COMPLETED)
                .findFirst();
        if (notReady.isPresent()) {
            return ApplicationError.businessRuleViolation(
                    "Family plan item is not ready to complete",
                    "QuestUser %d is %s with progress %.2f".formatted(
                            notReady.get().getId(),
                            notReady.get().getStatus(),
                            notReady.get().getProgress()
                    )
            );
        }

        for (var questUser : questUsers) {
            if (questUser.getStatus() == QuestStatus.COMPLETED) {
                continue;
            }
            questUser.complete();
            questUserRepository.save(questUser);
        }

        session.get().complete();
        collabQuestSessionRepository.save(session.get());
        return null;
    }

    private boolean hasBlockingActiveQuest(Long userId, Long questId) {
        return questUserRepository.findByQuestIdAndStatusIn(questId, ACTIVE_QUEST_USER_STATUSES)
                .stream()
                .filter(questUser -> questUser.getUserId().equals(userId))
                .anyMatch(this::isBlockingActiveQuestUser);
    }

    private boolean isBlockingActiveQuestUser(QuestUser questUser) {
        if (questUser.getCollaborativeSessionId() == null) {
            return true;
        }

        var session = collabQuestSessionRepository.findById(questUser.getCollaborativeSessionId());
        return session
                .map(value -> value.getStatus() == CollabQuestStatus.PENDING
                        || value.getStatus() == CollabQuestStatus.STARTED)
                .orElse(true);
    }

    private void cleanupFamilyPlanItemProgress(FamilyPlanItem item) {
        if (item.getCollaborativeSessionId() == null) {
            return;
        }

        questUserRepository.findByQuestId(item.getQuestId())
                .stream()
                .filter(questUser -> item.getCollaborativeSessionId().equals(
                        questUser.getCollaborativeSessionId()
                ))
                .filter(questUser -> questUser.getStatus() != QuestStatus.COMPLETED)
                .forEach(questUser -> {
                    activityUserRepository.deleteByQuestUserId(questUser.getId());
                    questUserRepository.deleteById(questUser.getId());
                });
    }
}
