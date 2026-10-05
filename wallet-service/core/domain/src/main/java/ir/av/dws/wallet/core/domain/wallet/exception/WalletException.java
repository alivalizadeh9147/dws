package ir.av.dws.wallet.core.domain.wallet.exception;

import ir.av.dws.wallet.core.domain.shared.exception.DomainException;

public class WalletException extends DomainException {
    public WalletException(String message) {
        super(message);
    }
}
