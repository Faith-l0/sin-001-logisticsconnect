package co.wethinkcode.logisticsconnect;

/** Thrown when the upstream hub data cannot be fetched. */
public class SourceUnavailableException extends RuntimeException {
    public SourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SourceUnavailableException(String message) {
        super(message);
    }
}
