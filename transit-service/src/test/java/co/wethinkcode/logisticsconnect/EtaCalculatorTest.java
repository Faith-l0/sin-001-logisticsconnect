package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EtaCalculatorTest {

    @Test
    void stageZeroIsTheBaseWindow() {
        EtaWindow w = EtaCalculator.window(0);
        assertEquals(24, w.minHours());
        assertEquals(48, w.maxHours());
    }

    @Test
    void eachStageAddsTwelveHoursToBothEnds() {
        EtaWindow w = EtaCalculator.window(1);
        assertEquals(36, w.minHours());
        assertEquals(60, w.maxHours());
    }

    @Test
    void stageEightIsTheLongestWindow() {
        EtaWindow w = EtaCalculator.window(8);
        assertEquals(120, w.minHours());
        assertEquals(144, w.maxHours());
    }

    @Test
    void windowWidthStaysConstant() {
        for (int stage = 0; stage <= 8; stage++) {
            EtaWindow w = EtaCalculator.window(stage);
            assertEquals(24, w.maxHours() - w.minHours());
        }
    }

    @Test
    void rejectsNegativeStage() {
        assertThrows(IllegalArgumentException.class, () -> EtaCalculator.window(-1));
    }

    @Test
    void rejectsStageAboveEight() {
        assertThrows(IllegalArgumentException.class, () -> EtaCalculator.window(9));
    }
}
