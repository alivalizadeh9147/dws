package ir.av.dws.wallet.application.queue;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record WalletOperation<T>(
        Supplier<T> operation,
        CompletableFuture<T> result
) {

    public static <T> WalletOperation<T> of(
            Supplier<T> operation
    ) {
        return new WalletOperation<>(
                operation,
                new CompletableFuture<>()
        );
    }
}