package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RewardTransactionResource;

public final class RewardTransactionResourceFromEntityAssembler {
    private RewardTransactionResourceFromEntityAssembler() {}

    public static RewardTransactionResource toResourceFromEntity(RewardTransaction entity) {
        return new RewardTransactionResource(
                entity.id(),
                entity.sourceType().name(),
                entity.sourceExecutionId(),
                entity.beneficiary().value(),
                entity.grantedReward().ecopoints(),
                entity.grantedReward().gems(),
                entity.occurredAt());
    }
}
