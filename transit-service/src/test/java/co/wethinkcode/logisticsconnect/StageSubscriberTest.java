package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests how incoming messages are handled, without needing a broker. */
class StageSubscriberTest {

    private final StageCache cache = new StageCache();
    private final ActiveMqStageSubscriber subscriber =
            new ActiveMqStageSubscriber("tcp://unused:0", "unused-topic", cache);

    @Test
    void validMessageUpdatesTheCache() {
        subscriber.handleText("{\"hubId\":\"H-500\",\"stage\":5}");
        assertEquals(5, cache.stageFor("H-500"));
    }

    @Test
    void malformedMessageIsIgnored() {
        subscriber.handleText("garbage");
        assertEquals(0, cache.stageFor("H-500"));
    }

    @Test
    void outOfRangeStageIsIgnored() {
        subscriber.handleText("{\"hubId\":\"H-500\",\"stage\":42}");
        assertEquals(0, cache.stageFor("H-500"));
    }

    @Test
    void nullMessageBodyIsIgnored() {
        subscriber.handleText(null);
        assertEquals(0, cache.stageFor("H-500"));
    }
}
