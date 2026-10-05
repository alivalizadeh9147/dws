package ir.av.dws.wallet.application.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
public class WalletOperationQueue {

    private final BlockingQueue<WalletOperation<?>> queue =
            new ArrayBlockingQueue<>(100_000);

    public <T> CompletableFuture<T> submit(
            Supplier<T> operation
    ) {

        WalletOperation<T> command =
                WalletOperation.of(operation);

        boolean accepted = queue.offer(command);

        if (!accepted) {
            throw new QueueFullException(
                    "Wallet operation queue is full"
            );
        }

        return command.result();
    }

    public WalletOperation<?> take()
            throws InterruptedException {

        return queue.take();
    }
}