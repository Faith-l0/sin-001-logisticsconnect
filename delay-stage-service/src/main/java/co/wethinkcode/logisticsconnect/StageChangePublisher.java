package co.wethinkcode.logisticsconnect;

/** Announces that a hub's delay stage has changed. */
@FunctionalInterface
public interface StageChangePublisher {
    void publish(String hubId, int stage) throws PublishFailedException;
}
