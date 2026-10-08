package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationStatus;
import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

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
    }
}
