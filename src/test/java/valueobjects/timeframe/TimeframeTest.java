package valueobjects.timeframe;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeframeTest {

    @Test
    void addTick() {
        Timeframe timeframe = new Timeframe(2);
        // Tickers can be added up to the specified limit
        assertEquals(1, timeframe.addTick(BigDecimal.ONE).size());
        assertEquals(2, timeframe.addTick(BigDecimal.TEN).size());
        LinkedList<Tick> ticks = timeframe.addTick(BigDecimal.ZERO);
        assertEquals(2, ticks.size());

        // The latest values are stored
        assertEquals(BigDecimal.ZERO, ticks.getLast().getValue());
        assertEquals(BigDecimal.TEN, ticks.getFirst().getValue());
    }

    @Test
    void crossover() {

        Timeframe timeframe = new Timeframe(2);
        timeframe.addTick(BigDecimal.ZERO);
        timeframe.addTick(BigDecimal.TEN);

        Timeframe timeframe2 = new Timeframe(2);
        timeframe2.addTick(BigDecimal.ZERO);
        timeframe2.addTick(BigDecimal.ONE);

        // timeframe just crossed ABOVE another timeframe
        assertEquals(1, timeframe.crossover(timeframe2));
        // timeframe just crossed BELOW another timeframe
        assertEquals(-1, timeframe2.crossover(timeframe));
        // no crossover between 2 timeframes
        assertEquals(0, timeframe.crossover(timeframe));

    }
}