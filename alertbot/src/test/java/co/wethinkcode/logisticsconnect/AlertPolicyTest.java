package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import co.wethinkcode.logisticsconnect.AlertPolicy.Decision;
import org.junit.jupiter.api.Test;

class AlertPolicyTest {

    @Test
    void crossingUpToTheThresholdRaisesAnAlert() {
        assertEquals(Decision.ALERT, AlertPolicy.decide(5, 6));
    }

    @Test
    void jumpingStraightPastTheThresholdRaisesAnAlert() {
        assertEquals(Decision.ALERT, AlertPolicy.decide(0, 8));
    }

    @Test
    void staysQuietWhileAlreadyAboveTheThreshold() {
        assertEquals(Decision.NONE, AlertPolicy.decide(6, 7));
        assertEquals(Decision.NONE, AlertPolicy.decide(8, 6));
    }

    @Test
    void staysQuietBelowTheThreshold() {
        assertEquals(Decision.NONE, AlertPolicy.decide(2, 3));
        assertEquals(Decision.NONE, AlertPolicy.decide(0, 0));
    }

    @Test
    void droppingBelowTheThresholdIsARecovery() {
        assertEquals(Decision.RECOVERED, AlertPolicy.decide(7, 5));
        assertEquals(Decision.RECOVERED, AlertPolicy.decide(6, 0));
    }

    @Test
    void thresholdIsSix() {
        assertEquals(6, AlertPolicy.THRESHOLD);
    }
}
