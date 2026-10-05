package ir.av.dws.wallet.core.domain.shared.exception;

import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;

public final class InsufficientBalanceException
        extends DomainException {

    public InsufficientBalanceException(
            WalletId walletId
    ) {
        super(
                "Insufficient balance for wallet: " + walletId
        );
    }
}