package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertBotTest {

    private final List<String> posts = new ArrayList<>();
    private final AlertBot bot = new AlertBot(posts::add);

    @Test
    void highStageRaisesOneAlert() {
        bot.onUpdate(new StageUpdate("H-500", 7));
        assertEquals(1, posts.size());
        assertTrue(posts.get(0).contains("H-500"));
        assertTrue(posts.get(0).contains("7"));
    }

    @Test
    void stayingHighDoesNotRepeatTheAlert() {
        bot.onUpdate(new StageUpdate("H-500", 7));
        bot.onUpdate(new StageUpdate("H-500", 8));
        assertEquals(1, posts.size());
    }

    @Test
    void lowStageIsQuiet() {
        bot.onUpdate(new StageUpdate("H-500", 3));
        assertTrue(posts.isEmpty());
    }

    @Test
    void droppingBackPostsARecovery() {
        bot.onUpdate(new StageUpdate("H-500", 7));
        bot.onUpdate(new StageUpdate("H-500", 2));
        assertEquals(2, posts.size());
        assertTrue(posts.get(1).toLowerCase().contains("easing"));
    }

    @Test
    void hubsAreTrackedSeparately() {
        bot.onUpdate(new StageUpdate("H-500", 7));
        bot.onUpdate(new StageUpdate("H-501", 6));
        assertEquals(2, posts.size());
    }
}
