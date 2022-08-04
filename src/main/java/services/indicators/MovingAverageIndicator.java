package services.indicators;

import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.function.BiFunction;

/**
 * In statistics, a moving average is a calculation used to analyze data points by creating a series of averages of
 * different subsets of the full data set. In finance, a moving average (MA) is a stock indicator that is commonly used
 * in technical analysis. The reason for calculating the moving average of a stock is to help smooth out the price data
 * by creating a constantly updated average price.
 *
 * <a href="https://www.investopedia.com/terms/m/movingaverage.asp">SMA explanation</a>
 */
public class MovingAverageIndicator implements BiFunction<Timeframe, Integer, Timeframe> {

    @Override
    public Timeframe apply(Timeframe timeframe, Integer period) {

        LinkedList<Tick> ticks = timeframe.getTicks();
        Timeframe movingAverageTimeFrame = new Timeframe(timeframe.size());
        for (int i = 0; i < ticks.size(); i++) {
            int startingIndex = i < period ? 0 : (i + 1 - period);
            LinkedList<Tick> periodTicks = new LinkedList<>(ticks.subList(startingIndex, i + 1));
            movingAverageTimeFrame.addTick(movingAverage(periodTicks));
        }
        return movingAverageTimeFrame;
    }

    private Tick movingAverage(LinkedList<Tick> ticks) {
        BigDecimal value = ticks.stream().map(Tick::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ticks.size()), 10, RoundingMode.HALF_EVEN);
        return new Tick(ticks.getLast().getTime(), value);
    }
}
