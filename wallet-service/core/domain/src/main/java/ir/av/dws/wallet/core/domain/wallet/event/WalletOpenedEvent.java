package ir.av.dws.wallet.core.domain.wallet.event;

import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record WalletOpenedEvent(UUID eventId,
                                Instant occurredOn,
                                Payload payload) implements DomainEvent<WalletOpenedEvent.Payload> {

    @Override
    public String eventType() {
        return "WALLET_OPENED_EVENT";
    }

    public record Payload() {

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

        public WalletOpenedEvent build() {
            return new WalletOpenedEvent(eventId, occurredOn, payload);
        }
    }
}
