package ir.av.dws.wallet.core.application.ports.outbound.eventpublisher;

import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;

import java.util.List;

public interface EventPublisher {

    void publishAll(DomainEvent<?>... events);

    void publishAll(List<DomainEvent<?>> events);
}
