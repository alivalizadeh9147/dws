package ir.av.dws.transaction.inbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InboxService {

    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper;

    public InboxService(InboxRepository inboxRepository, ObjectMapper objectMapper) {
        this.inboxRepository = inboxRepository;
        this.objectMapper = objectMapper;
    }

    public void process(String message) {
        try {
            RabbitMqEventDto dto = objectMapper.readValue(message, RabbitMqEventDto.class);
            InboxEntity inboxEntity = new InboxEntity();
            inboxEntity.setId(UUID.randomUUID());
            inboxEntity.setEventId(dto.getEventId());
            inboxEntity.setEventType(dto.getEventType());
            inboxEntity.setOccurredOn(dto.getOccurredOn());
            inboxEntity.setStatus(InboxStatus.RECEIVED);
            inboxEntity.setPayload(dto.getPayload());
            inboxEntity.setIdempotencyKey(dto.getIdempotencyKey());
            inboxRepository.save(inboxEntity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
