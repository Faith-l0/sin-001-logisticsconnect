package co.wethinkcode.logisticsconnect;

/** Thrown when a service this one depends on cannot be reached or misbehaves. */
public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }

    public DependencyUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
