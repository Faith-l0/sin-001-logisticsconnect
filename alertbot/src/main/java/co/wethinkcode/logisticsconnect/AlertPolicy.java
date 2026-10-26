package co.wethinkcode.logisticsconnect;

/** Pure rule for when a stage change deserves a public alert. */
public final class AlertPolicy {

    /** A hub at this delay stage or worse counts as seriously delayed. */
    public static final int THRESHOLD = 6;

    public enum Decision { NONE, ALERT, RECOVERED }

    private AlertPolicy() {
    }

    public static Decision decide(int previousStage, int newStage) {
        boolean wasHigh = previousStage >= THRESHOLD;
        boolean isHigh = newStage >= THRESHOLD;
        if (!wasHigh && isHigh) {
            return Decision.ALERT;
        }
        if (wasHigh && !isHigh) {
            return Decision.RECOVERED;
        }
        return Decision.NONE;
    }
}
