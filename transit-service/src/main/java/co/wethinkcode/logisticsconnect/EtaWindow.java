package co.wethinkcode.logisticsconnect;

/** Estimated arrival window, in hours from now. */
public record EtaWindow(int minHours, int maxHours) {
}
