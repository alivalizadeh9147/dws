package ir.av.dws.wallet.adapters.secondary.eventbus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.av.dws.wallet.adapters.secondary.eventbus.outbox.OutboxEntity;
import ir.av.dws.wallet.adapters.secondary.eventbus.outbox.OutboxRepository;
import ir.av.dws.wallet.adapters.secondary.eventbus.outbox.OutboxStatus;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class OutboxEventPublisher implements EventPublisher {

    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public OutboxEventPublisher(ObjectMapper objectMapper,
                                OutboxRepository outboxRepository) {
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    @Override
    @Transactional
    public void publishAll(DomainEvent<?>... events) {
        List<OutboxEntity> list = Stream.of(events).map(this::map).toList();
        outboxRepository.saveAll(list);
    }

    private OutboxEntity map(DomainEvent<?> domainEvent) {
        OutboxEntity entity = new OutboxEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventId(domainEvent.eventId());
        entity.setOccurredOn(domainEvent.occurredOn());
        entity.setEventType(domainEvent.eventType());
        entity.setStatus(OutboxStatus.PENDING);
        try {
            entity.setPayload(objectMapper.writeValueAsString(domainEvent.payload()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    @Transactional
    public void publishAll(List<DomainEvent<?>> events) {
        List<OutboxEntity> list = events.stream().map(this::map).toList();
        outboxRepository.saveAll(list);
    }
}
