package pe.greenminds.ecomind.community.application.internal.queryservices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.application.queryservices.EventParticipationQueryService;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.queries.SearchEventParticipationQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventParticipation;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRegistrationRepository;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class EventParticipationQueryServiceImpl
        implements EventParticipationQueryService {
    private final EventRepository events;
    private final EventRegistrationRepository registrations;
    private final CommunityRepository communities;
    private final CommunityActorGateway actors;

    public EventParticipationQueryServiceImpl(
            EventRepository events,
            EventRegistrationRepository registrations,
            CommunityRepository communities,
            CommunityActorGateway actors) {
        this.events = events;
        this.registrations = registrations;
        this.communities = communities;
        this.actors = actors;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Result<List<EventParticipation>, ApplicationError> handle(
            SearchEventParticipationQuery query) {
        if (!query.mine() && query.communityId() == null) {
            return Result.failure(ApplicationError.validationError(
                    "Community", "Community id is required"));
        }

        var now = Instant.now();
        var familyId = actors.getRegistrableFamilyId(query.requestedBy());
        var result = new ArrayList<EventParticipation>();

        for (var event : events.findAll(query.mine() ? null : query.communityId())) {
            var activeRegistrations = registrations.findByEventId(event.getId())
                    .stream()
                    .filter(registration ->
                            registration.status() == EventRegistrationStatus.REGISTERED)
                    .toList();

            var owned = activeRegistrations.stream()
                    .filter(registration ->
                            registration.userId().equals(query.requestedBy()))
                    .findFirst();

            var participation = activeRegistrations.stream()
                    .filter(registration -> registration.includes(query.requestedBy()))
                    .findFirst();

            boolean joined = participation.isPresent();

            boolean canCancelGroup = owned
                    .filter(registration ->
                            registration.registrationType() == EventRegistrationType.FAMILY)
                    .isPresent();

            if (query.mine() && !joined && !canCancelGroup) {
                continue;
            }

            int participantCount = Math.toIntExact(activeRegistrations.stream()
                    .mapToLong(EventRegistration::activeParticipantCount)
                    .sum());

            Long registrationId = owned
                    .map(EventRegistration::id)
                    .orElseGet(() -> participation
                            .map(EventRegistration::id)
                            .orElse(null));

            String communityName = communities.findById(event.getCommunityId())
                    .map(community -> community.getName())
                    .orElse("");

            result.add(new EventParticipation(
                    event,
                    communityName,
                    actors.getUserName(event.getAuthorId()),
                    participantCount,
                    Math.addExact(event.getCapacity(), participantCount),
                    registrationId,
                    joined,
                    canCancelGroup,
                    event.getAuthorId().equals(query.requestedBy()),
                    event.getChatExpiresAt(),
                    joined && event.isChatOpen(now),
                    familyId));
        }

        return Result.success(List.copyOf(result));
    }
}