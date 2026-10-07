package pe.greenminds.ecomind.gamification.application.outboundservices;

import java.util.List;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingParticipant;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public interface RankingParticipantDirectory {
  List<RankingParticipant> participants(RankingType type, UserId requestedBy);
}
