package trading.strategies;

import trading.indicators.MovingAverageIndicator;
import trading.timeframe.Tick;
import trading.timeframe.Timeframe;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

/**
 * Crossovers strategy with moving average indicators (long and short).
 * When the shorter-term MA crosses above the longer-term MA, it's a buy signal, as it indicates that the trend is
 * shifting up. This is known as a golden cross. Meanwhile, when the shorter-term MA crosses below the longer-term MA,
 * it's a sell signal, as it indicates that the trend is shifting down. This is known as a dead/death cross.
 *
 * <a href="https://www.investopedia.com/articles/active-trading/052014/how-use-moving-average-buy-stocks.asp">Moving average stock trading</a>
 */
public class MovingAverageCrossoverStrategy implements TradingStrategy {

    private final MovingAverageIndicator shortMovingAverageIndicator;
    private final MovingAverageIndicator longMovingAverageIndicator;

    public MovingAverageCrossoverStrategy(int shortPeriod, int longPeriod) {
        this.shortMovingAverageIndicator = new MovingAverageIndicator(shortPeriod);
        this.longMovingAverageIndicator = new MovingAverageIndicator(longPeriod);
    }

    @Override
    public Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy() {
        return (timeframe) -> {

            Timeframe<BigDecimal> shortMovingAverage = shortMovingAverageIndicator.apply(timeframe);
            Timeframe<BigDecimal> longMovingAverage = longMovingAverageIndicator.apply(timeframe);

            Iterator<Tick<BigDecimal>> shortTickIterator = shortMovingAverage.getTicks().descendingIterator();
            BigDecimal currentShortMovingAverage = shortTickIterator.next().getValue();
            BigDecimal previousShortMovingAverage = shortTickIterator.next().getValue();

            Iterator<Tick<BigDecimal>> longTickIterator = longMovingAverage.getTicks().descendingIterator();
            BigDecimal currentLongMovingAverage = longTickIterator.next().getValue();
            BigDecimal previousLongMovingAverage = longTickIterator.next().getValue();


            int currentMovingAverageSignum = currentShortMovingAverage.subtract(currentLongMovingAverage).signum();
            int previousMovingAverageSignum = previousShortMovingAverage.subtract(previousLongMovingAverage).signum();


            if (currentMovingAverageSignum != 0 && currentMovingAverageSignum != previousMovingAverageSignum) {
                TradingSignal tradingSignal = currentMovingAverageSignum > 0 ? TradingSignal.BUY : TradingSignal.SELL;
                return Optional.of(tradingSignal);
            }

            return Optional.empty();
        };
    }
}
