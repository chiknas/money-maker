package services.indicators;

import org.junit.jupiter.api.Test;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExponentialMovingAverageIndicatorTest {

    /**
     * Generated with the help of:
     * <a href="https://goodcalculators.com/exponential-moving-average-calculator/">EMA calculator</a>
     */
    @Test
    void name() {
        Timeframe integerTimeframe = new Timeframe(9);
        integerTimeframe.addTick(BigDecimal.valueOf(2));
        integerTimeframe.addTick(BigDecimal.valueOf(4));
        integerTimeframe.addTick(BigDecimal.valueOf(6));
        integerTimeframe.addTick(BigDecimal.valueOf(8));
        integerTimeframe.addTick(BigDecimal.valueOf(12));
        integerTimeframe.addTick(BigDecimal.valueOf(14));
        integerTimeframe.addTick(BigDecimal.valueOf(16));
        integerTimeframe.addTick(BigDecimal.valueOf(18));
        integerTimeframe.addTick(BigDecimal.valueOf(20));

        ExponentialMovingAverageIndicator exponentialMovingAverageIndicator = new ExponentialMovingAverageIndicator();
        Timeframe movingAverageTimeFrame = exponentialMovingAverageIndicator.apply(integerTimeframe, 3);

        assertEquals(9, movingAverageTimeFrame.size());
        // Smoothing Factor (SF) = 2 / (9+1) = 0.2
        // first item is averaged with itself so the average is itself EMA(1) = Price(1) = 10
        assertEquals(BigDecimal.valueOf(2), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(0).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(3), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(1).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(4.5), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(2).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(6.25), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(3).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(9.125), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(4).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(11.5625), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(5).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(13.78125), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(6).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(15.890625), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(7).getValue().doubleValue()).stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(17.9453125), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(8).getValue().doubleValue()).stripTrailingZeros());
    }
}