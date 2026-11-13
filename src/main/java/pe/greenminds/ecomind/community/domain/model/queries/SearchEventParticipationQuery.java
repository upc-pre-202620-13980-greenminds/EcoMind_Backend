package pe.greenminds.ecomind.community.domain.model.queries;

public record SearchEventParticipationQuery(
        Long communityId,
        Long requestedBy,
        boolean mine) {
}