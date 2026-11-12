package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(
        name = "community_event_chat_messages",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"event_id", "sender_id", "client_id"}),
        indexes = @Index(
                name = "idx_event_chat_messages_event_id_id",
                columnList = "event_id,id"))
public class EventChatMessagePersistenceEntity
        extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    protected EventChatMessagePersistenceEntity() {
    }

    public EventChatMessagePersistenceEntity(
            Long eventId,
            Long senderId,
            String senderName,
            String text,
            Instant sentAt,
            UUID clientId) {
        this.eventId = eventId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.sentAt = sentAt;
        this.clientId = clientId;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public UUID getClientId() {
        return clientId;
    }
}