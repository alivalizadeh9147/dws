package ir.av.dws.wallet.core.application.usecases.wallet.exception;

import ir.av.dws.wallet.core.domain.wallet.exception.WalletException;

public class WalletNotFoundException extends WalletException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}
