package trading.timeframe;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeframeTest {

    @Test
    void addTick() {
        Timeframe<BigDecimal> timeframe = new Timeframe<>(2);
        // Tickers can be added up to the specified limit
        assertEquals(1, timeframe.addTick(BigDecimal.ONE).size());
        assertEquals(2, timeframe.addTick(BigDecimal.TEN).size());
        LinkedList<Tick<BigDecimal>> ticks = timeframe.addTick(BigDecimal.ZERO);
        assertEquals(2, ticks.size());

        // The latest values are stored
        assertEquals(BigDecimal.ZERO, ticks.getLast().getValue());
        assertEquals(BigDecimal.TEN, ticks.getFirst().getValue());
    }
}