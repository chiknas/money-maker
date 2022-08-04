package services.strategies;

import com.google.inject.Inject;
import properties.PropertiesService;
import properties.ThreeEmaCrossoverStrategyProperties;
import services.indicators.ExponentialMovingAverageIndicator;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

/**
 * The three EMAs can give stronger confirmation than just two EMAs crossover. It can also give a better context to
 * the price action in relation to the three EMA lines displayed on the chart. Three EMAs crossing above the price
 * at the same time is a strong bullish signal, while three EMA crossing below the price at the same time is a strong bearish signal.
 *
 * <a href="https://www.brokerxplorer.com/article/the-ultimate-3-ema-crossover-strategy-revealed-1856">3 EMA cross over strategy details</a>
 */
public class ThreeEmaCrossoverStrategy implements TradingStrategy {

    private final PropertiesService propertiesService;
    private final ExponentialMovingAverageIndicator exponentialMovingAverageIndicator;

    @Inject
    public ThreeEmaCrossoverStrategy(PropertiesService propertiesService, ExponentialMovingAverageIndicator exponentialMovingAverageIndicator) {
        this.propertiesService = propertiesService;
        this.exponentialMovingAverageIndicator = exponentialMovingAverageIndicator;
    }

    @Override
    public boolean enabled() {
        return propertiesService.loadProperties(ThreeEmaCrossoverStrategyProperties.class)
                .map(ThreeEmaCrossoverStrategyProperties::getEnabled)
                .orElse(false);
    }

    @Override
    public String name() {
        return "3EmaCrossover";
    }

    @Override
    public Duration periodLength() {
        return propertiesService.loadProperties(ThreeEmaCrossoverStrategyProperties.class)
                .map(ThreeEmaCrossoverStrategyProperties::getPeriodLength)
                .orElse(Duration.ofHours(1));
    }

    @Override
    public Function<Timeframe, Optional<TradingSignal>> strategy() {
        return (timeframe) -> {
            // Load settings
            Optional<ThreeEmaCrossoverStrategyProperties> threeEmaCrossoverStrategyProperties = propertiesService.loadProperties(ThreeEmaCrossoverStrategyProperties.class);
            Integer shortMovingAveragePeriod = threeEmaCrossoverStrategyProperties.map(ThreeEmaCrossoverStrategyProperties::getShortPeriod).orElse(20);
            Integer mediumMovingAveragePeriod = threeEmaCrossoverStrategyProperties.map(ThreeEmaCrossoverStrategyProperties::getMediumPeriod).orElse(50);
            Integer longMovingAveragePeriod = threeEmaCrossoverStrategyProperties.map(ThreeEmaCrossoverStrategyProperties::getLongPeriod).orElse(200);

            // Calculate EMAs (short, medium and long)
            Timeframe shortEma = exponentialMovingAverageIndicator.apply(timeframe, shortMovingAveragePeriod);
            Timeframe mediumEma = exponentialMovingAverageIndicator.apply(timeframe, mediumMovingAveragePeriod);
            Timeframe longEma = exponentialMovingAverageIndicator.apply(timeframe, longMovingAveragePeriod);

            // Check for any kind of crossover between them
            int shortCrossover = shortEma.crossover(longEma);
            int mediumCrossover = mediumEma.crossover(longEma);
            int shortMediumCrossover = shortEma.crossover(mediumEma);

            // On a crossover check if the 3 EMAs are going in the same direction and in the correct order
            // (short is highest and long is lowest)
            // In this scenario trigger a signal based on the direction the graph is moving.
            if (shortCrossover != 0 || mediumCrossover != 0 || shortMediumCrossover != 0) {
                return getOrderEmaDirection(shortEma, mediumEma, longEma)
                        .map(direction -> direction > 0 ? TradingSignal.BUY : TradingSignal.SELL);
            }
            return Optional.empty();
        };
    }

    /**
     * If the 3 EMAs are in order it will return the direction they are moving at.
     * Positive(+) = the graph/price is moving UP
     * Negative(-) = the graph/price is moving down
     */
    private Optional<Integer> getOrderEmaDirection(Timeframe shortEma, Timeframe mediumEma, Timeframe longEma) {
        BigDecimal shortEmaValue = shortEma.getTicks().getLast().getValue();
        BigDecimal mediumEmaValue = mediumEma.getTicks().getLast().getValue();
        BigDecimal longEmaValue = longEma.getTicks().getLast().getValue();

        int shortMedium = shortEmaValue.subtract(mediumEmaValue).signum();
        int mediumLong = mediumEmaValue.subtract(longEmaValue).signum();

        boolean isSamePrice = shortMedium == 0;
        boolean isOrdered = shortMedium == mediumLong;

        return isOrdered && !isSamePrice ? Optional.of(shortMedium) : Optional.empty();
    }
}
