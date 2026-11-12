package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.EventRegistrationCommandService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.community.domain.model.commands.*;
import pe.greenminds.ecomind.community.domain.repositories.*;
import pe.greenminds.ecomind.shared.application.result.*;
import java.util.Set;
import java.time.Instant;

@Service
public class EventRegistrationCommandServiceImpl implements EventRegistrationCommandService {
    private final EventRepository events;
    private final EventRegistrationRepository registrations;
    private final CommunityActorGateway actors;

    public EventRegistrationCommandServiceImpl(EventRepository eventRepository, EventRegistrationRepository eventRegistrationRepository,
            CommunityActorGateway communityActorGateway) {
        events = eventRepository;
        registrations = eventRegistrationRepository;
        actors = communityActorGateway;
    }

    @Transactional
    @Override
    public Result<EventRegistration, ApplicationError> handle(RegisterForEventCommand command) {
        var event = events.findByIdForUpdate(command.eventId());

        if (event.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Event", String.valueOf(command.eventId())));
        }

        if (!event.get().isChatOpen(Instant.now())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_REGISTRATION_CLOSED",
                    "Registration is closed for this event"));
        }

        var previousRegistration = registrations.findByEventIdAndUserId(
                command.eventId(),
                command.userId());

        if (previousRegistration
                .filter(registration -> registration.status() == EventRegistrationStatus.REGISTERED)
                .isPresent()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_REGISTRATION_ALREADY_ACTIVE",
                    "The user already manages an active registration"));
        }

        Set<Long> participants;

        if (command.registrationType() == EventRegistrationType.INDIVIDUAL) {
            if (command.familyId() != null) {
                return Result.failure(ApplicationError.validationError(
                        "Event registration",
                        "Individual registration cannot include family id"));
            }

            participants = Set.of(command.userId());
        } else if (command.registrationType() == EventRegistrationType.FAMILY) {
            if (command.familyId() == null) {
                return Result.failure(ApplicationError.validationError(
                        "Event registration",
                        "Family id is required"));
            }

            try {
                participants = actors.requireFamilyParentAndGetMembers(
                        command.userId(),
                        command.familyId());
            } catch (SecurityException exception) {
                return Result.failure(ApplicationError.forbidden(
                        "FAMILY_REGISTRATION_FORBIDDEN",
                        exception.getMessage()));
            }
        } else {
            return Result.failure(ApplicationError.validationError(
                    "Event registration",
                    "Registration type must be INDIVIDUAL or FAMILY"));
        }

        var existingRegistrations = registrations.findByEventId(command.eventId());

        boolean participantAlreadyRegistered = existingRegistrations.stream()
                .filter(registration -> registration.status() == EventRegistrationStatus.REGISTERED)
                .anyMatch(registration -> participants.stream().anyMatch(registration::includes));

        if (participantAlreadyRegistered) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_PARTICIPANT_ALREADY_REGISTERED",
                    "One or more participants already belong to this event"));
        }

        int count = participants.size();

        if (count > event.get().getCapacity()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_CAPACITY_EXCEEDED",
                    "There are not enough available places"));
        }

        var registration = registrations.save(new EventRegistration(
                previousRegistration.map(EventRegistration::id).orElse(null),
                command.eventId(),
                command.userId(),
                command.registrationType(),
                command.familyId(),
                count,
                EventRegistrationStatus.REGISTERED,
                participants));

        events.save(event.get().reservePlaces(count));

        return Result.success(registration);
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(CancelEventRegistrationCommand command) {
        var event = events.findByIdForUpdate(command.eventId());

        if (event.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Event", String.valueOf(command.eventId())));
        }

        var registration = registrations.findById(command.registrationId())
                .filter(existingRegistration ->
                        existingRegistration.eventId().equals(command.eventId()));

        if (registration.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Event registration",
                    String.valueOf(command.registrationId())));
        }

        var current = registration.get();

        if (!current.userId().equals(command.requestedBy())) {
            return Result.failure(ApplicationError.forbidden(
                    "EVENT_REGISTRATION_CANCEL_FORBIDDEN",
                    "Only the registration owner may cancel the group"));
        }

        if (current.status() == EventRegistrationStatus.CANCELLED) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_REGISTRATION_ALREADY_CANCELLED",
                    "Registration is already cancelled"));
        }

        if (event.get().getAuthorId().equals(command.requestedBy())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_CREATOR_CANNOT_LEAVE",
                    "The creator must delete the event instead of leaving it"));
        }

        int releasedPlaces = current.activeParticipantCount();

        registrations.save(current.cancel());
        events.save(event.get().releasePlaces(releasedPlaces));

        return Result.success(null);
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(LeaveEventCommand command) {
        var event = events.findByIdForUpdate(command.eventId());

        if (event.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("Event", String.valueOf(command.eventId())));
        }

        if (event.get().getAuthorId().equals(command.requestedBy())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "EVENT_CREATOR_CANNOT_LEAVE",
                    "The creator must delete the event instead of leaving it"));
        }

        var registration = registrations.findByEventId(command.eventId()).stream()
                .filter(existingRegistration -> existingRegistration.includes(command.requestedBy()))
                .findFirst();

        if (registration.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Event participation",
                    command.eventId() + ":" + command.requestedBy()));
        }

        registrations.save(registration.get().removeParticipant(command.requestedBy()));
        events.save(event.get().releasePlaces(1));

        return Result.success(null);
    }
}
