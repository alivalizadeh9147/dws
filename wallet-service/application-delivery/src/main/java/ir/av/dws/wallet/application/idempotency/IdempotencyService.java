package ir.av.dws.wallet.application.idempotency;

public interface IdempotencyService {

    void execute(
            String idempotencyKey,
            Runnable action
    );
}
