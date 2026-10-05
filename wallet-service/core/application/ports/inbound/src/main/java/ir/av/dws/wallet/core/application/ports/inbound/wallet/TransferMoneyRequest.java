package ir.av.dws.wallet.core.application.ports.inbound.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.BaseRequest;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyRequest(UUID sourceWalletId,
                                   UUID destinationWalletId,
                                   BigDecimal amount) implements BaseRequest {
}
