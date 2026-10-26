package co.wethinkcode.logisticsconnect;

/** Where alerts go. The real version would call a social media API. */
@FunctionalInterface
public interface AlertPoster {
    void post(String text);
}
