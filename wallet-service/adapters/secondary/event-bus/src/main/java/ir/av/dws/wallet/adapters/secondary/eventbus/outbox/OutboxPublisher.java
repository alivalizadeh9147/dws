package ir.av.dws.wallet.adapters.secondary.eventbus.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.av.dws.wallet.adapters.secondary.eventbus.rabbitmq.RabbitMQConfig;
import ir.av.dws.wallet.adapters.secondary.eventbus.rabbitmq.RabbitMqEventDto;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(OutboxEntity event) throws JsonProcessingException {
        RabbitMqEventDto dto = new RabbitMqEventDto(
                event.getEventType(), event.getEventId(), event.getOccurredOn(),
                event.getPayload(), event.getId()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EVENTS_EXCHANGE,
                event.getEventType(), objectMapper.writeValueAsString(dto)
        );
    }
}
