package services.strategies.tradingstrategies;

import com.google.inject.Inject;
import properties.GoldenCrossStrategyProperties;
import properties.PropertiesService;
import services.indicators.MovingAverageIndicator;
import valueobjects.timeframe.Timeframe;

import java.time.Duration;
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

    private final GoldenCrossStrategyProperties properties;
    private final MovingAverageIndicator movingAverageIndicator;

    @Inject
    public GoldenCrossStrategy(PropertiesService propertiesService, MovingAverageIndicator movingAverageIndicator) {
        this.movingAverageIndicator = movingAverageIndicator;
        this.properties = propertiesService.loadProperties(GoldenCrossStrategyProperties.class).orElseThrow(() ->
                new IllegalStateException("Trade config must be setup before the system starts trading.")
        );
    }

    @Override
    public boolean enabled() {
        return properties.getEnabled();
    }

    @Override
    public String name() {
        return "GoldenCross";
    }

    @Override
    public String exitStrategyName() {
        return properties.getExitStrategy();
    }

    @Override
    public Duration periodLength() {
        return properties.getPeriodLength();
    }

    @Override
    public Integer timeframeSize() {
        return properties.getTimeframeSize();
    }

    @Override
    public Function<Timeframe, Optional<TradingSignal>> strategy() {
        return (timeframe) -> {

            Integer shortMovingAveragePeriod = properties.getShortPeriod();
            Integer longMovingAveragePeriod = properties.getLongPeriod();

            Timeframe shortMovingAverage = movingAverageIndicator.apply(timeframe, shortMovingAveragePeriod);
            Timeframe longMovingAverage = movingAverageIndicator.apply(timeframe, longMovingAveragePeriod);

            int crossover = shortMovingAverage.crossover(longMovingAverage);
            if (crossover != 0) {
                TradingSignal tradingSignal = crossover > 0 ? TradingSignal.BUY : TradingSignal.SELL;
                return Optional.of(tradingSignal);
            }

            return Optional.empty();
        };
    }
}
