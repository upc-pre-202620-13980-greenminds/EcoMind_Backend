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
        var event = events.findById(command.eventId());
        if (event.isEmpty())
            return Result.failure(ApplicationError.notFound("Event", String.valueOf(command.eventId())));
        var previousRegistration = registrations.findByEventIdAndUserId(command.eventId(), command.userId());
        if (previousRegistration.filter(existingRegistration -> existingRegistration.status() == EventRegistrationStatus.REGISTERED)
                .isPresent())
            return Result.failure(
                    ApplicationError.conflict("Event registration", "The user already has an active registration"));
        int count;
        if (command.registrationType() == EventRegistrationType.INDIVIDUAL) {
            if (command.familyId() != null)
                return Result.failure(ApplicationError.validationError("Event registration",
                        "Individual registration cannot include family id"));
            count = 1;
        } else if (command.registrationType() == EventRegistrationType.FAMILY) {
            if (command.familyId() == null)
                return Result.failure(ApplicationError.validationError("Event registration", "Family id is required"));
            try {
                count = actors.requireFamilyParentAndCount(command.userId(), command.familyId());
            } catch (SecurityException exception) {
                return Result.failure(ApplicationError.forbidden("FAMILY_REGISTRATION_FORBIDDEN", exception.getMessage()));
            }
        } else {
            return Result.failure(ApplicationError.validationError("Event registration",
                    "Registration type must be INDIVIDUAL or FAMILY"));
        }
        int occupied = registrations.findByEventId(command.eventId()).stream()
                .filter(existingRegistration -> existingRegistration.status() == EventRegistrationStatus.REGISTERED)
                .mapToInt(EventRegistration::participantCount).sum();
        if (occupied + count > event.get().getCapacity())
            return Result.failure(ApplicationError.conflict("Event capacity", "Event capacity exceeded"));
        try {
            return Result.success(registrations.save(new EventRegistration(previousRegistration.map(EventRegistration::id).orElse(null), command.eventId(), command.userId(),
                    command.registrationType(), command.familyId(), count, EventRegistrationStatus.REGISTERED)));
        } catch (Exception exception) {
            return Result.failure(ApplicationError.unexpected("Event registration", exception.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<Void, ApplicationError> handle(CancelEventRegistrationCommand command) {
        var registration = registrations.findById(command.registrationId()).filter(existingRegistration -> existingRegistration.eventId().equals(command.eventId()));
        if (registration.isEmpty())
            return Result.failure(ApplicationError.notFound("Event registration", String.valueOf(command.registrationId())));
        if (!registration.get().userId().equals(command.requestedBy()))
            return Result.failure(ApplicationError.forbidden("EVENT_REGISTRATION_CANCEL_FORBIDDEN",
                    "Only the registration owner may cancel it"));
        if (registration.get().status() == EventRegistrationStatus.CANCELLED)
            return Result.failure(ApplicationError.businessRuleViolation("EVENT_REGISTRATION_ALREADY_CANCELLED",
                    "Registration is already cancelled"));
        var eventRegistration = registration.get();
        registrations.save(new EventRegistration(eventRegistration.id(), eventRegistration.eventId(), eventRegistration.userId(), eventRegistration.registrationType(), eventRegistration.familyId(),
                eventRegistration.participantCount(), EventRegistrationStatus.CANCELLED));
        return Result.success(null);
    }
}
