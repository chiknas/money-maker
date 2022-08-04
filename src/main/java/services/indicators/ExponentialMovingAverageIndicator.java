package services.indicators;

import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.function.BiFunction;

/**
 * The Exponential Moving Average (EMA) is a weighted moving average. Which means that unlike a simple moving average
 * where the values of the far past have the same weight in the calculation as more recent values, a weighted moving
 * average gives greater significance to more recent values than older one.
 *
 * <a href="https://nullbeans.com/how-to-calculate-the-exponential-moving-average-ema/">EMA explanation</a>
 */
public class ExponentialMovingAverageIndicator implements BiFunction<Timeframe, Integer, Timeframe> {
    @Override
    public Timeframe apply(Timeframe timeframe, Integer period) {
        // Smoothing Factor = 2 / (Number of time periods + 1)
        BigDecimal smoothingFactor = BigDecimal.valueOf(2)
                .divide(
                        BigDecimal.valueOf(period)
                                .add(BigDecimal.ONE), 10, RoundingMode.HALF_EVEN);

        LinkedList<Tick> ticks = timeframe.getTicks();
        Timeframe movingAverageTimeFrame = new Timeframe(timeframe.size());
        for (int i = 0; i < ticks.size(); i++) {
            LinkedList<Tick> periodTicks = new LinkedList<>(ticks.subList(0, i + 1));
            movingAverageTimeFrame.addTick(exponentialMovingAverage(periodTicks, smoothingFactor));
        }
        return movingAverageTimeFrame;
    }

    private Tick exponentialMovingAverage(LinkedList<Tick> ticks, BigDecimal smoothingFactor) {
        if (ticks.size() == 1) {
            return ticks.getFirst();
        }

        LinkedList<Tick> previousTimeframe = new LinkedList<>(ticks.subList(0, ticks.size() - 1));
        Tick previousEma = exponentialMovingAverage(previousTimeframe, smoothingFactor);
        BigDecimal ema = (ticks.getLast().getValue().multiply(smoothingFactor)).add(
                BigDecimal.ONE.subtract(smoothingFactor).multiply(previousEma.getValue())
        );
        return new Tick(ticks.getLast().getTime(), ema);
    }


}
