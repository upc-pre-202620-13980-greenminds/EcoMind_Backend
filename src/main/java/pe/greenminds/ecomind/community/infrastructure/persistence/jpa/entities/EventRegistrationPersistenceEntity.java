package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "community_event_registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "event_id", "user_id" }) })
public class EventRegistrationPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long eventId;
    @Column(nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRegistrationType registrationType;
    private Long familyId;
    @Column(nullable = false)
    private Integer participantCount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRegistrationStatus status;
    @ElementCollection
    @CollectionTable(
            name = "community_event_registration_participants",
            joinColumns = @JoinColumn(name = "registration_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"registration_id", "user_id"}))
    @Column(name = "user_id", nullable = false)
    private Set<Long> participantIds = new HashSet<>();

    protected EventRegistrationPersistenceEntity() {
    }

    public EventRegistrationPersistenceEntity(Long eventId, Long userId, EventRegistrationType type, Long familyId, Integer count) {
        this.eventId = eventId;
        this.userId = userId;
        this.registrationType = type;
        this.familyId = familyId;
        this.participantCount = count;
        this.status = EventRegistrationStatus.REGISTERED;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public EventRegistrationType getRegistrationType() {
        return registrationType;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public EventRegistrationStatus getStatus() {
        return status;
    }

    public void cancel() {
        status = EventRegistrationStatus.CANCELLED;
        participantIds.clear();
    }

    public void reactivate(EventRegistrationType registrationType, Long familyId, Integer participantCount) {
        this.registrationType = registrationType;
        this.familyId = familyId;
        this.participantCount = participantCount;
        status = EventRegistrationStatus.REGISTERED;
    }

    public Set<Long> getParticipantIds() {
        return Set.copyOf(participantIds);
    }

    public void replaceParticipants(Set<Long> participants) {
        participantIds.clear();
        participantIds.addAll(participants);
    }
}
