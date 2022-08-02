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
public class ExponentialMovingAverageIndicator implements BiFunction<Timeframe<BigDecimal>, Integer, Timeframe<BigDecimal>> {
    @Override
    public Timeframe<BigDecimal> apply(Timeframe<BigDecimal> timeframe, Integer period) {
        // Smoothing Factor = 2 / (Number of time periods + 1)
        BigDecimal smoothingFactor = BigDecimal.valueOf(2)
                .divide(
                        BigDecimal.valueOf(period)
                                .add(BigDecimal.ONE), 10, RoundingMode.HALF_EVEN);

        LinkedList<Tick<BigDecimal>> ticks = timeframe.getTicks();
        Timeframe<BigDecimal> movingAverageTimeFrame = new Timeframe<>(timeframe.size());
        for (int i = 0; i < ticks.size(); i++) {
            int startingIndex = i < period ? 0 : (i + 1 - period);
            LinkedList<Tick<BigDecimal>> periodTicks = new LinkedList<>(ticks.subList(startingIndex, i + 1));
            movingAverageTimeFrame.addTick(exponentialMovingAverage(periodTicks, smoothingFactor));
        }
        return movingAverageTimeFrame;
    }

    private Tick<BigDecimal> exponentialMovingAverage(LinkedList<Tick<BigDecimal>> ticks, BigDecimal smoothingFactor) {
        if (ticks.size() == 1) {
            return ticks.getFirst();
        }

        LinkedList<Tick<BigDecimal>> previousTimeframe = new LinkedList<>(ticks.subList(0, ticks.size() - 1));
        Tick<BigDecimal> previousEma = exponentialMovingAverage(previousTimeframe, smoothingFactor);
        BigDecimal ema = (ticks.getLast().getValue().multiply(smoothingFactor)).add(
                BigDecimal.ONE.subtract(smoothingFactor).multiply(previousEma.getValue())
        );
        return new Tick<>(ticks.getLast().getTime(), ema);
    }


}
