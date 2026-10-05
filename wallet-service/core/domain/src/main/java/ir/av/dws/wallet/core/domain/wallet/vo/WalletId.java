package ir.av.dws.wallet.core.domain.wallet.vo;

import ir.av.dws.wallet.core.domain.wallet.exception.InvalidWalletOperationException;
import ir.av.dws.wallet.core.domain.shared.vo.Identity;

import java.util.UUID;

public record WalletId(UUID value) implements Identity {

    public WalletId {
        if (value == null) {
            throw new InvalidWalletOperationException("Wallet Id Not Valid");
        }
    }

    public static WalletId open() {
        return new WalletId(UUID.randomUUID());
    }
}
