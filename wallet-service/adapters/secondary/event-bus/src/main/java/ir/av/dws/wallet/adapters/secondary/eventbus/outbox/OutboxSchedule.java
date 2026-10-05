package ir.av.dws.wallet.adapters.secondary.eventbus.outbox;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxSchedule {

    private final OutboxService outboxService;
    private final OutboxPublisher outboxPublisher;

    public OutboxSchedule(OutboxService outboxService,
                          OutboxPublisher outboxPublisher) {
        this.outboxService = outboxService;
        this.outboxPublisher = outboxPublisher;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {

        List<OutboxEntity> events =
                outboxService.claimBatch(100);

        for (OutboxEntity event : events) {
            try {
                outboxPublisher.publish(event);
                outboxService.setCompleted(event);
            } catch (Exception e) {
                outboxService.setPending(event);
            }
        }
    }
}
