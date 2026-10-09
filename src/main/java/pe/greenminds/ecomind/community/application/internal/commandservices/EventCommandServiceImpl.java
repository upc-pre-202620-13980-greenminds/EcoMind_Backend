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

@Service
public class EventCommandServiceImpl implements EventCommandService {
    private final EventRepository events;
    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final EventRegistrationRepository registrations;
    private final PostRepository posts;
    private final CommunityActorGateway actors;
    private final ApplicationEventPublisher publisher;

    public EventCommandServiceImpl(EventRepository eventRepository, CommunityRepository communityRepository, CommunityMembershipRepository communityMembershipRepository,
            EventRegistrationRepository registrations, PostRepository postRepository, CommunityActorGateway communityActorGateway,
            ApplicationEventPublisher publisher) {
        events = eventRepository;
        communities = communityRepository;
        memberships = communityMembershipRepository;
        this.registrations = registrations;
        posts = postRepository;
        actors = communityActorGateway;
        this.publisher = publisher;
    }

    @Transactional
    @Override
    public Result<Event, ApplicationError> handle(CreateEventCommand command) {
        if (!communities.existsById(command.communityId()))
            return Result.failure(ApplicationError.notFound("Community", String.valueOf(command.communityId())));
        try {
            actors.requireParent(command.authorId());
            var event = events.save(new Event(null, command.communityId(), command.authorId(), command.name(), command.description(), command.date(),
                    command.startTime(), command.location(), command.latitude(), command.longitude(), command.capacity(), command.imageUrl()));
            registrations.save(
                    new EventRegistration(null, event.getId(), command.authorId(), EventRegistrationType.INDIVIDUAL, null, 1,
                            EventRegistrationStatus.REGISTERED));
            posts.save(new Post(null, command.communityId(), null, "A new event was created: " + event.getName(),
                    "EVENT_CREATED", command.imageUrl(), event.getId()));
            publisher.publishEvent(
                    new EventCreatedEvent(event.getId(), event.getCommunityId(), event.getAuthorId(), event.getName()));
            return Result.success(event);
        } catch (SecurityException exception) {
            return Result.failure(ApplicationError.forbidden("EVENT_CREATOR_NOT_PARENT", exception.getMessage()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Result.failure(ApplicationError.validationError("Event", exception.getMessage()));
        } catch (Exception exception) {
            return Result.failure(ApplicationError.unexpected("Event creation", exception.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(DeleteEventCommand command) {
        var found = events.findById(command.eventId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("Event", String.valueOf(command.eventId())));
        boolean creator = found.get().getAuthorId().equals(command.requestedBy());
        boolean admin = memberships.findByCommunityIdAndUserId(found.get().getCommunityId(), command.requestedBy())
                .map(membership -> CommunityRole.ADMIN == membership.role()).orElse(false);
        if (!creator && !admin)
            return Result.failure(ApplicationError.forbidden("EVENT_DELETE_FORBIDDEN",
                    "Only the event creator or community administrator may delete it"));
        events.delete(found.get());
        return Result.success(null);
    }
}
