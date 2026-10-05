package ir.av.dws.wallet.core.application.ports.inbound.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.BaseRequest;

public record OpenWalletRequest(String name) implements BaseRequest {
}
