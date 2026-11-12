package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.util.HashSet;
import java.util.Set;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

public record EventRegistration(
        Long id,
        Long eventId,
        Long userId,
        EventRegistrationType registrationType,
        Long familyId,
        Integer participantCount,
        EventRegistrationStatus status,
        Set<Long> participantIds) {

    public EventRegistration {
        if (eventId == null || userId == null || participantCount == null || participantCount < 1) {
            throw new IllegalArgumentException("Event, user and participant count are required");
        }

        if (registrationType == null || status == null) {
            throw new IllegalArgumentException("Registration type and status are required");
        }

        participantIds = participantIds == null ? Set.of() : Set.copyOf(participantIds);

        if (participantIds.stream().anyMatch(participantId -> participantId < 1)) {
            throw new IllegalArgumentException("Participant identifiers must be positive");
        }

        if (status == EventRegistrationStatus.CANCELLED && !participantIds.isEmpty()) {
            throw new IllegalArgumentException("Cancelled registrations cannot have active participants");
        }

        if (participantIds.size() > participantCount) {
            throw new IllegalArgumentException("Active participants cannot exceed the registration count");
        }
    }

    public EventRegistration(
            Long id,
            Long eventId,
            Long userId,
            EventRegistrationType registrationType,
            Long familyId,
            Integer participantCount,
            EventRegistrationStatus status) {
        this(
                id,
                eventId,
                userId,
                registrationType,
                familyId,
                participantCount,
                status,
                status == EventRegistrationStatus.REGISTERED
                        && registrationType == EventRegistrationType.INDIVIDUAL
                        ? Set.of(userId)
                        : Set.of());
    }

    public boolean includes(Long participantId) {
        return status == EventRegistrationStatus.REGISTERED
                && participantIds.contains(participantId);
    }

    public int activeParticipantCount() {
        if (status == EventRegistrationStatus.CANCELLED) {
            return 0;
        }

        return participantIds.isEmpty() ? participantCount : participantIds.size();
    }

    public EventRegistration cancel() {
        return new EventRegistration(
                id,
                eventId,
                userId,
                registrationType,
                familyId,
                participantCount,
                EventRegistrationStatus.CANCELLED,
                Set.of());
    }

    public EventRegistration removeParticipant(Long participantId) {
        if (!includes(participantId)) {
            throw new IllegalArgumentException("The user is not an active participant");
        }

        var remainingParticipants = new HashSet<>(participantIds);
        remainingParticipants.remove(participantId);

        if (remainingParticipants.isEmpty()) {
            return cancel();
        }

        return new EventRegistration(
                id,
                eventId,
                userId,
                registrationType,
                familyId,
                participantCount,
                EventRegistrationStatus.REGISTERED,
                remainingParticipants);
    }
}