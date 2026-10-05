package ir.av.dws.wallet.core.domain.shared.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent<P> {

    String eventType();

    UUID eventId();

    Instant occurredOn();

    P payload();
}
