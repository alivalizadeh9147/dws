package ir.av.dws.wallet.core.application.ports.inbound.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.BaseRequest;

import java.util.UUID;

public record OpenWalletRequest(UUID userId) implements BaseRequest {
}
