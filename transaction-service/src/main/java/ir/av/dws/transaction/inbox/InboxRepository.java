package ir.av.dws.transaction.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxEntity, UUID> {

    boolean existsByEventId(UUID eventId);
}
