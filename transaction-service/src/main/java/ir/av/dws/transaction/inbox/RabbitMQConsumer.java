package ir.av.dws.transaction.inbox;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    private final InboxService inboxService;

    public RabbitMQConsumer(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @RabbitListener(queues = {
            "wallet.money.transferred",
            "wallet.balance.decreased",
            "wallet.balance.increased"
    })
    public void consumeTransferred(String message) {
        inboxService.process(message);
    }
}
