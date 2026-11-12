package pe.greenminds.ecomind.community.application.internal.commandservices;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.EventCommandService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.*;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.model.events.EventCreatedEvent;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.repositories.*;
import pe.greenminds.ecomind.shared.application.result.*;
import java.time.Instant;
import pe.greenminds.ecomind.community.application.commandservices.EventLifecycleService;

@Service
public class EventCommandServiceImpl implements EventCommandService {
    private final EventRepository events;
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final EventRegistrationRepository registrations;
    private final PostRepository posts;
    private final CommunityActorGateway actors;
    private final ApplicationEventPublisher publisher;
    private final EventLifecycleService lifecycle;

    public EventCommandServiceImpl(EventRepository eventRepository, CommunityRepository communityRepository, CommunityMembershipRepository communityMembershipRepository, EventRegistrationRepository registrations, PostRepository postRepository, CommunityActorGateway communityActorGateway, ApplicationEventPublisher publisher, EventLifecycleService lifecycle) {
        events = eventRepository;
        communities = communityRepository;
        memberships = communityMembershipRepository;
        this.registrations = registrations;
        posts = postRepository;
        actors = communityActorGateway;
        this.publisher = publisher;
        this.lifecycle = lifecycle;
    }

    @Transactional
    @Override
    public Result<Event, ApplicationError> handle(CreateEventCommand command) {
        if (!communities.existsById(command.communityId())) {
            return Result.failure(ApplicationError.notFound(
                    "Community", String.valueOf(command.communityId())));
        }

        Event candidate;

        try {
            actors.requireParent(command.authorId());

            candidate = new Event(
                    null,
                    command.communityId(),
                    command.authorId(),
                    command.name(),
                    command.description(),
                    command.date(),
                    command.startTime(),
                    command.location(),
                    command.latitude(),
                    command.longitude(),
                    command.capacity(),
                    command.imageUrl()).reservePlaces(1);
        } catch (SecurityException exception) {
            return Result.failure(ApplicationError.forbidden(
                    "EVENT_CREATOR_NOT_PARENT", exception.getMessage()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(ApplicationError.validationError(
                    "Event", exception.getMessage()));
        }

        if (!candidate.isChatOpen(Instant.now())) {
            return Result.failure(ApplicationError.validationError(
                    "Event schedule",
                    "The event chat expiration must be in the future"));
        }

        var event = events.save(candidate);

        registrations.save(new EventRegistration(
                null,
                event.getId(),
                command.authorId(),
                EventRegistrationType.INDIVIDUAL,
                null,
                1,
                EventRegistrationStatus.REGISTERED));

        posts.save(new Post(
                null,
                event.getCommunityId(),
                event.getAuthorId(),
                "A new event was created: " + event.getName(),
                "EVENT_CREATED",
                command.imageUrl(),
                event.getId()));

        publisher.publishEvent(new EventCreatedEvent(
                event.getId(),
                event.getCommunityId(),
                event.getAuthorId(),
                event.getName()));

        return Result.success(event);
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(DeleteEventCommand command) {
        var found = events.findByIdForUpdate(command.eventId());

        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Event", String.valueOf(command.eventId())));
        }

        boolean creator = found.get().getAuthorId().equals(command.requestedBy());

        boolean admin = memberships.findByCommunityIdAndUserId(
                        found.get().getCommunityId(),
                        command.requestedBy())
                .map(membership -> CommunityRole.ADMIN == membership.role())
                .orElse(false);

        if (!creator && !admin) {
            return Result.failure(ApplicationError.forbidden(
                    "EVENT_DELETE_FORBIDDEN",
                    "Only the event creator or community administrator may delete it"));
        }

        lifecycle.delete(command.eventId());

        return Result.success(null);
    }

    @Transactional
    @Override
    public Result<Event, ApplicationError> handle(UpdateEventCommand command) {
        var found = events.findByIdForUpdate(command.eventId());

        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Event", String.valueOf(command.eventId())));
        }

        Event current = found.get();

        if (!current.getAuthorId().equals(command.requestedBy())) {
            return Result.failure(ApplicationError.forbidden(
                    "EVENT_UPDATE_FORBIDDEN",
                    "Only the event creator may update it"));
        }

        boolean changesSchedule = command.date() != null || command.startTime() != null;

        if (changesSchedule && !current.isChatOpen(Instant.now())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_SCHEDULE_CLOSED",
                    "The schedule cannot change after the event chat has expired"));
        }

        Event updated;

        try {
            updated = current.updateDetails(command);
        } catch (IllegalArgumentException exception) {
            return Result.failure(ApplicationError.validationError(
                    "Event", exception.getMessage()));
        }

        if (changesSchedule && !updated.isChatOpen(Instant.now())) {
            return Result.failure(ApplicationError.validationError(
                    "Event schedule",
                    "The new schedule would leave the event chat expired"));
        }

        if (command.capacity() != null) {
            long occupied = registrations.findByEventId(command.eventId())
                    .stream()
                    .filter(registration ->
                            registration.status() == EventRegistrationStatus.REGISTERED)
                    .mapToLong(EventRegistration::activeParticipantCount)
                    .sum();

            if (updated.getCapacity() < occupied) {
                return Result.failure(ApplicationError.conflict(
                        "Event capacity",
                        "Capacity cannot be lower than the number of registered participants"));
            }

            if (occupied > 0) {
                updated = updated.reservePlaces(Math.toIntExact(occupied));
            }
        }

        return Result.success(events.save(updated));
    }
}
