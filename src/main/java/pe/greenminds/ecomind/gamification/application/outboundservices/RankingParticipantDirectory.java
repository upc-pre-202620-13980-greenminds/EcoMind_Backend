package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingParticipant;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

import java.util.List;

public interface RankingParticipantDirectory {
    List<RankingParticipant> participants(RankingType type, UserId requestedBy);
}
