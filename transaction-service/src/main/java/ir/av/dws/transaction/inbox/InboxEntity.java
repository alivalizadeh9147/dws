package ir.av.dws.transaction.inbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "INBOX", uniqueConstraints = {@UniqueConstraint(name = "UQ_EVENT_ID_IDEMPOTENCY_KEY",
        columnNames = {"event_id", "idempotency_key"})})
public class InboxEntity {

    @Id
    private UUID id;

    private String eventType;

    private UUID eventId;

    private Instant occurredOn;

    private String payload;

    @Enumerated(EnumType.STRING)
    private InboxStatus status;

    private UUID idempotencyKey;

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public void setOccurredOn(Instant occurredOn) {
        this.occurredOn = occurredOn;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public InboxStatus getStatus() {
        return status;
    }

    public void setStatus(InboxStatus status) {
        this.status = status;
    }
}
