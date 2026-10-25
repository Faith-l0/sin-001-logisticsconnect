package co.wethinkcode.logisticsconnect;

/** A stage change received from the message topic. */
public record StageUpdate(String hubId, int stage) {
}
