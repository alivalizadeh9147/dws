package ir.av.dws.wallet.application.queue;

public class QueueFullException extends RuntimeException {
    public QueueFullException(String message) {
        super(message);
    }
}
