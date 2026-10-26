package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests how incoming messages are handled, without needing a broker. */
class AlertSubscriberTest {

    private final List<String> posts = new ArrayList<>();
    private final ActiveMqAlertSubscriber subscriber =
            new ActiveMqAlertSubscriber("tcp://unused:0", "unused-topic", new AlertBot(posts::add));

    @Test
    void highStageMessageTriggersAnAlert() {
        subscriber.handleText("{\"hubId\":\"H-500\",\"stage\":7}");
        assertEquals(1, posts.size());
    }

    @Test
    void malformedMessageIsIgnored() {
        subscriber.handleText("garbage");
        assertTrue(posts.isEmpty());
    }

    @Test
    void nullMessageBodyIsIgnored() {
        subscriber.handleText(null);
        assertTrue(posts.isEmpty());
    }
}
