package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestMemberQueryService;
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

    public CollabQuestMemberQueryServiceImpl(CollabQuestMemberRepository repository) {
        this.repository = repository;
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
        return query.status() == null
                ? repository.findByUserId(query.userId())
                : repository.findByUserIdAndStatus(query.userId(), query.status());
    }
}
