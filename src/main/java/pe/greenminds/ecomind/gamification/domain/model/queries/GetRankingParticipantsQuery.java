package pe.greenminds.ecomind.gamification.domain.model.queries;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public record GetRankingParticipantsQuery(
        RankingType type, UserId requestedBy, int page, int size) {}
