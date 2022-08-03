package services.strategies;

import com.google.inject.Inject;
import properties.GoldenCrossStrategyProperties;
import properties.PropertiesService;
import services.indicators.MovingAverageIndicator;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.time.Duration;
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
public class GoldenCrossStrategy implements TradingStrategy {

    private final PropertiesService propertiesService;
    private final MovingAverageIndicator movingAverageIndicator;

    @Inject
    public GoldenCrossStrategy(PropertiesService propertiesService, MovingAverageIndicator movingAverageIndicator) {
        this.propertiesService = propertiesService;
        this.movingAverageIndicator = movingAverageIndicator;
    }

    @Override
    public String name() {
        return "GoldenCross";
    }

    @Override
    public Duration periodLength() {
        return propertiesService.loadProperties(GoldenCrossStrategyProperties.class)
                .map(GoldenCrossStrategyProperties::getPeriodLength)
                .orElse(Duration.ofSeconds(1));
    }

    @Override
    public Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy() {
        return (timeframe) -> {

            Optional<GoldenCrossStrategyProperties> goldenCrossStrategyProperties = propertiesService.loadProperties(GoldenCrossStrategyProperties.class);
            Integer shortMovingAveragePeriod = goldenCrossStrategyProperties.map(GoldenCrossStrategyProperties::getShortPeriod).orElse(50);
            Integer longMovingAveragePeriod = goldenCrossStrategyProperties.map(GoldenCrossStrategyProperties::getLongPeriod).orElse(100);

            Timeframe<BigDecimal> shortMovingAverage = movingAverageIndicator.apply(timeframe, shortMovingAveragePeriod);
            Timeframe<BigDecimal> longMovingAverage = movingAverageIndicator.apply(timeframe, longMovingAveragePeriod);

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
