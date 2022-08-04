package services.strategies;

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

    private final PropertiesService propertiesService;
    private final MovingAverageIndicator movingAverageIndicator;

    @Inject
    public GoldenCrossStrategy(PropertiesService propertiesService, MovingAverageIndicator movingAverageIndicator) {
        this.propertiesService = propertiesService;
        this.movingAverageIndicator = movingAverageIndicator;
    }

    @Override
    public boolean enabled() {
        return propertiesService.loadProperties(GoldenCrossStrategyProperties.class)
                .map(GoldenCrossStrategyProperties::getEnabled)
                .orElse(false);
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
    public Function<Timeframe, Optional<TradingSignal>> strategy() {
        return (timeframe) -> {

            Optional<GoldenCrossStrategyProperties> goldenCrossStrategyProperties = propertiesService.loadProperties(GoldenCrossStrategyProperties.class);
            Integer shortMovingAveragePeriod = goldenCrossStrategyProperties.map(GoldenCrossStrategyProperties::getShortPeriod).orElse(50);
            Integer longMovingAveragePeriod = goldenCrossStrategyProperties.map(GoldenCrossStrategyProperties::getLongPeriod).orElse(100);

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
