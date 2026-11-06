package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestMemberQueryService;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestInvitation;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestInvitationsByUserQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.CollabQuestMember;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMemberByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMembersBySessionQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMembersByUserQuery;
import pe.greenminds.ecomind.quests.domain.repositories.CollabQuestMemberRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CollabQuestMemberQueryServiceImpl implements CollabQuestMemberQueryService {
    private final CollabQuestMemberRepository repository;

    private final CollabQuestSessionRepository sessionRepository;
    private final QuestRepository questRepository;

    public CollabQuestMemberQueryServiceImpl(CollabQuestMemberRepository repository,
            CollabQuestSessionRepository sessionRepository, QuestRepository questRepository) {
        this.repository = repository;
        this.sessionRepository = sessionRepository;
        this.questRepository = questRepository;
    }

    @Override
    public Optional<CollabQuestMember> handle(GetCollabQuestMemberByIdQuery query) {
        return Optional.ofNullable(repository.findById(query.memberId()));
    }

    @Override
    public List<CollabQuestMember> handle(GetCollabQuestMembersBySessionQuery query) {
        return repository.findBySessionId(query.sessionId());
    }

    @Override
    public List<CollabQuestMember> handle(GetCollabQuestMembersByUserQuery query) {
        var members = query.status() == null
                ? repository.findByUserId(query.userId())
                : repository.findByUserIdAndStatus(query.userId(), query.status());
        return members.stream()
                .filter(member -> member.getStatus() != CollabMemberStatus.PENDING || isActionable(member))
                .toList();
    }

    @Override
    public List<CollabQuestInvitation> handle(GetCollabQuestInvitationsByUserQuery query) {
        return repository.findByUserIdAndStatus(query.userId(), CollabMemberStatus.PENDING).stream()
                .flatMap(member -> sessionRepository.findById(member.getSessionId())
                        .filter(session -> session.getStatus() == CollabQuestStatus.PENDING)
                        .flatMap(session -> questRepository.findById(session.getQuestId())
                                .map(quest -> new CollabQuestInvitation(member.getId(), session.getId(),
                                        quest.getId(), quest.getTitle(), session.getOwnerId(), member.getUserId(),
                                        member.getStatus(), session.getStatus())))
                        .stream())
                .toList();
    }

    private boolean isActionable(CollabQuestMember member) {
        return sessionRepository.findById(member.getSessionId())
                .map(session -> session.getStatus() == CollabQuestStatus.PENDING)
                .orElse(false);
    }
}
