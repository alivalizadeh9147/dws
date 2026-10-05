package ir.av.dws.wallet.application.queue;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class WalletOperationWorker {

    private final WalletOperationQueue queue;

    public WalletOperationWorker(
            WalletOperationQueue queue
    ) {
        this.queue = queue;
    }

    @PostConstruct
    public void start() {

        for (int i = 0; i < 4; i++) {

            Thread.startVirtualThread(() -> {

                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        WalletOperation<?> command =
                                queue.take();

                        execute(command);

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    private <T> void execute(
            WalletOperation<T> command
    ) {

        try {

            T result = command.operation().get();

            command.result().complete(result);

        } catch (Exception e) {

            command.result().completeExceptionally(e);
        }
    }
}