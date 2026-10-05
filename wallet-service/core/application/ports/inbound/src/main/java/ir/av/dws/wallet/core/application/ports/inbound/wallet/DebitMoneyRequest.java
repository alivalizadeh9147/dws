package ir.av.dws.wallet.core.application.ports.inbound.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.BaseRequest;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitMoneyRequest(UUID userId, BigDecimal amount) implements BaseRequest {
}
