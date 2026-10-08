package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;
import pe.greenminds.ecomind.quests.domain.model.events.FamilyPlanCompletedEvent;

import java.time.OffsetDateTime;
import java.util.Objects;

public class FamilyPlan extends AbstractDomainAggregateRoot<FamilyPlan> {
    @Getter
    @Setter
    private Long id;

    private Long familyId;
    private Long ownerUserId;
    private FamilyPlanStatus status;
    private OffsetDateTime completedAt;

    public FamilyPlan(Long id, Long familyId, Long ownerUserId, FamilyPlanStatus status, OffsetDateTime completedAt) {
        this.id = id;
        this.familyId = Objects.requireNonNull(familyId, "familyId must not be null");
        this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.completedAt = completedAt;
    }

    public FamilyPlan(Long id, Long familyId, Long ownerUserId, FamilyPlanStatus status) {
        this(id, familyId, ownerUserId, status, null);
    }

    public FamilyPlan(Long familyId, Long ownerUserId) {
        this(null, familyId, ownerUserId, FamilyPlanStatus.DRAFT);
    }

    public void activate() {
        if (status != FamilyPlanStatus.DRAFT) {
            throw new IllegalStateException("Family plan must be DRAFT");
        }
        status = FamilyPlanStatus.ACTIVE;
    }

    public void cancel() {
        if (status != FamilyPlanStatus.ACTIVE) {
            throw new IllegalStateException("Family plan must be ACTIVE");
        }
        status = FamilyPlanStatus.CANCELLED;
    }

    public void complete() {
        if (status != FamilyPlanStatus.ACTIVE) {
            throw new IllegalStateException("Family plan must be ACTIVE");
        }
        status = FamilyPlanStatus.COMPLETED;
        completedAt = OffsetDateTime.now();
        registerDomainEvent(new FamilyPlanCompletedEvent(
                id, familyId, ownerUserId, completedAt
        ));
    }

    public Long getFamilyId() { return familyId; }
    public Long getOwnerUserId() { return ownerUserId; }
    public FamilyPlanStatus getStatus() { return status; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
}
