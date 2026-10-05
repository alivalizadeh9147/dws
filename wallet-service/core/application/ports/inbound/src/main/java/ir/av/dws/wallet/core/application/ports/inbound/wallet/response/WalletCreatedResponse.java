package ir.av.dws.wallet.core.application.ports.inbound.wallet.response;

import java.util.UUID;

public record WalletCreatedResponse(UUID walletId, String name) {
}
