package co.wethinkcode.logisticsconnect;

/** Thrown when hub-service cannot be reached or answers unexpectedly. */
public class HubServiceUnavailableException extends RuntimeException {
    public HubServiceUnavailableException(String message) {
        super(message);
    }

    public HubServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
