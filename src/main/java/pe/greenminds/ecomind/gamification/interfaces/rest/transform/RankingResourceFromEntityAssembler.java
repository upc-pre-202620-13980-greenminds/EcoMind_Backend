package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RankingEntryResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.RankingTransactionResource;

public final class RankingResourceFromEntityAssembler {
    private RankingResourceFromEntityAssembler() {}

    public static RankingEntryResource toResourceFromEntity(RankingEntry entry) {
        return new RankingEntryResource(
                entry.beneficiaryId(), entry.displayName(), entry.totalEcopoints());
    }

    public static RankingTransactionResource toResourceFromEntity(RankingTransaction entry) {
        return new RankingTransactionResource(
                entry.id(), entry.beneficiaryId(), entry.ecopoints(), entry.occurredAt());
    }
}
