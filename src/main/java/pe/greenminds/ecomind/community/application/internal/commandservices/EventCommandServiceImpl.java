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

    public EventCommandServiceImpl(EventRepository e, CommunityRepository c, CommunityMembershipRepository m,
            EventRegistrationRepository registrations, PostRepository p, CommunityActorGateway a,
            ApplicationEventPublisher publisher) {
        events = e;
        communities = c;
        memberships = m;
        this.registrations = registrations;
        posts = p;
        actors = a;
        this.publisher = publisher;
    }

    @Transactional
    public Result<Event, ApplicationError> handle(CreateEventCommand c) {
        if (!communities.existsById(c.communityId()))
            return Result.failure(ApplicationError.notFound("Community", String.valueOf(c.communityId())));
        try {
            actors.requireParent(c.authorId());
            var event = events.save(new Event(null, c.communityId(), c.authorId(), c.name(), c.description(), c.date(),
                    c.startTime(), c.location(), c.latitude(), c.longitude(), c.capacity(), c.imageUrl()));
            registrations.save(
                    new EventRegistration(null, event.getId(), c.authorId(), EventRegistrationType.INDIVIDUAL, null, 1,
                            EventRegistrationStatus.REGISTERED));
            posts.save(new Post(null, c.communityId(), null, "A new event was created: " + event.getName(),
                    "EVENT_CREATED", c.imageUrl(), event.getId()));
            publisher.publishEvent(
                    new EventCreatedEvent(event.getId(), event.getCommunityId(), event.getAuthorId(), event.getName()));
            return Result.success(event);
        } catch (SecurityException e) {
            return Result.failure(ApplicationError.forbidden("EVENT_CREATOR_NOT_PARENT", e.getMessage()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.failure(ApplicationError.validationError("Event", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Event creation", e.getMessage()));
        }
    }

    @Transactional
    public Result<Void, ApplicationError> handle(DeleteEventCommand c) {
        var found = events.findById(c.eventId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("Event", String.valueOf(c.eventId())));
        boolean creator = found.get().getAuthorId().equals(c.requestedBy());
        boolean admin = memberships.findByCommunityIdAndUserId(found.get().getCommunityId(), c.requestedBy())
                .map(m -> CommunityRole.ADMIN == m.role()).orElse(false);
        if (!creator && !admin)
            return Result.failure(ApplicationError.forbidden("EVENT_DELETE_FORBIDDEN",
                    "Only the event creator or community administrator may delete it"));
        events.delete(found.get());
        return Result.success(null);
    }
}
