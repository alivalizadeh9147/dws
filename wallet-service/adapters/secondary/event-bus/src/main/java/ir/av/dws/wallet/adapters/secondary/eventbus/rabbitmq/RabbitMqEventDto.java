package ir.av.dws.wallet.adapters.secondary.eventbus.rabbitmq;

import java.time.Instant;
import java.util.UUID;

public record RabbitMqEventDto(String eventType,
                               UUID eventId,
                               Instant occurredOn,
                               String payload,
                               UUID idempotencyKey) {
}
