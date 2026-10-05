package ir.av.dws.wallet.core.domain.wallet.event;

import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record TransferredMoneyEvent(UUID eventId,
                                    Instant occurredOn,
                                    Payload payload) implements DomainEvent<TransferredMoneyEvent.Payload> {
    @Override
    public String eventType() {
        return "TRANSFERRED_MONEY_EVENT";
    }

    public record Payload(UUID sourceWalletId,
                          UUID sourceUserId,
                          UUID destinationWalletId,
                          UUID destinationUserId,
                          String amount) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID eventId;
        private Instant occurredOn;
        private Payload payload;

        private Builder() {
        }

        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder occurredOn(Instant occurredOn) {
            this.occurredOn = occurredOn;
            return this;
        }

        public Builder payload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public TransferredMoneyEvent build() {
            return new TransferredMoneyEvent(eventId, occurredOn, payload);
        }
    }
}
