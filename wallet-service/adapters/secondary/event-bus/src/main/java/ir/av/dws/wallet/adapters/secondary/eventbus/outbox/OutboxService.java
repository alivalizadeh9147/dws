package ir.av.dws.wallet.adapters.secondary.eventbus.outbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxService {

    private final OutboxRepository repository;

    public OutboxService(OutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<OutboxEntity> claimBatch(int batchSize) {

        List<OutboxEntity> events =
                repository.findAndLockPending(batchSize);

        events.forEach(event ->
                event.setStatus(OutboxStatus.PROCESSING)
        );

        return events;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setPending(OutboxEntity event) {
        event.setStatus(OutboxStatus.PENDING);
        repository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setCompleted(OutboxEntity event) {
        event.setStatus(OutboxStatus.COMPLETED);
        repository.save(event);
    }
}
