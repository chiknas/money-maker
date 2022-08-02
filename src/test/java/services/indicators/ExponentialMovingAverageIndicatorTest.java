package services.indicators;

import org.junit.jupiter.api.Test;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExponentialMovingAverageIndicatorTest {

    @Test
    void apply() {
        Timeframe<BigDecimal> integerTimeframe = new Timeframe<>(4);
        integerTimeframe.addTick(BigDecimal.valueOf(10));
        integerTimeframe.addTick(BigDecimal.valueOf(11));
        integerTimeframe.addTick(BigDecimal.valueOf(12));
        integerTimeframe.addTick(BigDecimal.valueOf(13));

        ExponentialMovingAverageIndicator exponentialMovingAverageIndicator = new ExponentialMovingAverageIndicator();
        Timeframe<BigDecimal> movingAverageTimeFrame = exponentialMovingAverageIndicator.apply(integerTimeframe, 3);

        assertEquals(4, movingAverageTimeFrame.size());
        // Smoothing Factor (SF) = 2 / (3+1) = 0.5
        // first item is averaged with itself so the average is itself EMA(1) = Price(1) = 10
        assertEquals(BigDecimal.valueOf(10.0), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(0).getValue().doubleValue()));
        assertEquals(BigDecimal.valueOf(11.25), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(2).getValue().doubleValue()));
        assertEquals(BigDecimal.valueOf(12.25), BigDecimal.valueOf(movingAverageTimeFrame.getTicks().get(3).getValue().doubleValue()));
    }
}