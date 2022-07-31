package services.indicators;

import org.junit.jupiter.api.Test;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingAverageIndicatorTest {

    @Test
    void apply() {
        Timeframe<BigDecimal> integerTimeframe = new Timeframe<>(5);
        integerTimeframe.addTick(BigDecimal.valueOf(2));
        integerTimeframe.addTick(BigDecimal.valueOf(4));
        integerTimeframe.addTick(BigDecimal.valueOf(8));
        integerTimeframe.addTick(BigDecimal.valueOf(9));
        integerTimeframe.addTick(BigDecimal.valueOf(1));

        MovingAverageIndicator movingAverageIndicator = new MovingAverageIndicator();
        Timeframe<BigDecimal> movingAverageTimeFrame = movingAverageIndicator.apply(integerTimeframe, 2);

        assertEquals(5, movingAverageTimeFrame.size());
        // first item is averaged with itself so the average is itself
        assertEquals(BigDecimal.valueOf(2), movingAverageTimeFrame.getTicks().get(0).getValue());
        // (4 + 2) / 2 = 3
        assertEquals(BigDecimal.valueOf(3), movingAverageTimeFrame.getTicks().get(1).getValue());
        // (8 + 4) / 2 = 6
        assertEquals(BigDecimal.valueOf(6), movingAverageTimeFrame.getTicks().get(2).getValue());
        // (9 + 8) / 2 = 8.5
        assertEquals(BigDecimal.valueOf(8.5), movingAverageTimeFrame.getTicks().get(3).getValue());
        // (1 + 9) / 2 = 5
        assertEquals(BigDecimal.valueOf(5), movingAverageTimeFrame.getTicks().get(4).getValue());
    }
}