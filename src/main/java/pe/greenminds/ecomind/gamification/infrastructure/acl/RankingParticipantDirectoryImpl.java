package pe.greenminds.ecomind.gamification.infrastructure.acl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.outboundservices.CommunityServiceClient;
import pe.greenminds.ecomind.gamification.application.outboundservices.RankingParticipantDirectory;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingParticipant;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

import java.util.List;

@Component
public class RankingParticipantDirectoryImpl implements RankingParticipantDirectory {
    private final UsersContextFacade users;
    private final CommunityServiceClient community;

    public RankingParticipantDirectoryImpl(
            UsersContextFacade users, CommunityServiceClient community) {
        this.users = users;
        this.community = community;
    }

    public List<RankingParticipant> participants(RankingType type, UserId requestedBy) {
        if (type == RankingType.LOCAL) {
            var local = community.findLocalCommunity(requestedBy.value());
            if (local.isEmpty()) return List.of();
            if (!community.isMember(local.get(), requestedBy.value()))
                throw new AccessDeniedException("Local community membership required");
            return community.findMembers(local.get()).stream()
                    .map(m -> new RankingParticipant(m.userId(), m.displayName()))
                    .toList();
        }
        var entries =
                switch (type) {
                    case GLOBAL -> users.rankingUsers();
                    case FRIENDS -> users.rankingFriends(requestedBy.value());
                    case FAMILIES -> users.rankingFamilies();
                    case LOCAL -> throw new IllegalStateException("Local ranking handled above");
                };
        return entries.stream()
                .map(entry -> new RankingParticipant(entry.id(), entry.displayName()))
                .toList();
    }
}
