package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardHistoryEntry;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RewardHistoryResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RewardHistoryResource.BeneficiaryResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RewardHistoryResource.RewardResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RewardHistoryResource.SourceResource;

public final class RewardHistoryResourceFromEntityAssembler {
    private RewardHistoryResourceFromEntityAssembler() {}

    public static RewardHistoryResource toResourceFromEntity(RewardHistoryEntry entry) {
        return new RewardHistoryResource(
                entry.id(),
                new SourceResource(entry.source().type().name(), entry.source().executionId()),
                new BeneficiaryResource(
                        entry.beneficiary().type().name(), entry.beneficiary().id()),
                toResource(entry.baseReward()),
                toResource(entry.grantedReward()),
                entry.occurredAt());
    }

    private static RewardResource toResource(Reward reward) {
        return new RewardResource(reward.ecopoints(), reward.experience(), reward.gems());
    }
}
