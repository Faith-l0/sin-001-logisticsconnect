package co.wethinkcode.logisticsconnect;

/** Thrown when a stage change could not be published to the message broker. */
public class PublishFailedException extends RuntimeException {
    public PublishFailedException(String message) {
        super(message);
    }

    public PublishFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
