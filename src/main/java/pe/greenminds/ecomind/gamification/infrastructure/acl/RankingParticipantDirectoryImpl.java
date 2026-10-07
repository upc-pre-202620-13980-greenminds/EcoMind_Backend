package pe.greenminds.ecomind.gamification.infrastructure.acl;

import java.util.List;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.gamification.application.outboundservices.RankingParticipantDirectory;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingParticipant;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

@Component
public class RankingParticipantDirectoryImpl implements RankingParticipantDirectory {
  private final UsersContextFacade users;
  public RankingParticipantDirectoryImpl(UsersContextFacade users) { this.users = users; }

  public List<RankingParticipant> participants(RankingType type, UserId requestedBy) {
    var entries = switch (type) {
      case GLOBAL -> users.rankingUsers();
      case FRIENDS -> users.rankingFriends(requestedBy.value());
      case FAMILIES -> users.rankingFamilies();
      case LOCAL -> throw new IllegalArgumentException("Local ranking requires Community membership integration");
    };
    return entries.stream().map(entry -> new RankingParticipant(entry.id(), entry.displayName())).toList();
  }
}
