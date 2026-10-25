package co.wethinkcode.logisticsconnect;

/** Pure ETA rule: a base window, pushed back by a fixed amount per delay stage. */
public final class EtaCalculator {

    static final int BASE_MIN_HOURS = 24;
    static final int BASE_MAX_HOURS = 48;
    static final int HOURS_PER_STAGE = 12;
    static final int MAX_STAGE = 8;

    private EtaCalculator() {
    }

    public static EtaWindow window(int stage) {
        if (stage < 0 || stage > MAX_STAGE) {
            throw new IllegalArgumentException("Stage must be between 0 and " + MAX_STAGE + ", got " + stage);
        }
        int delay = stage * HOURS_PER_STAGE;
        return new EtaWindow(BASE_MIN_HOURS + delay, BASE_MAX_HOURS + delay);
    }
}
