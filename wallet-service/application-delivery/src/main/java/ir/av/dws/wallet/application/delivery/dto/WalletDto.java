package ir.av.dws.wallet.application.delivery.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletDto(UUID id, UUID userId, BigDecimal balance) {
}
