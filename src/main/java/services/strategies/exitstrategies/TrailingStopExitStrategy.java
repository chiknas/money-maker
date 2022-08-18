package services.strategies.exitstrategies;

import com.google.inject.Inject;
import database.entities.TradeEntity;
import properties.PropertiesService;
import properties.TrailingStopExitStrategyProperties;
import services.strategies.tradingstrategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrailingStopExitStrategy implements ExitStrategy {

    public static final String NAME = "TrailingStop";

    private final PropertiesService propertiesService;
    private final TradeService tradeService;

    @Inject
    public TrailingStopExitStrategy(PropertiesService propertiesService, TradeService tradeService) {
        this.propertiesService = propertiesService;
        this.tradeService = tradeService;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public BiFunction<BigInteger, Timeframe, Optional<TradingStrategy.TradingSignal>> strategy() {
        return (exitStrategyId, timeframe) -> {
            TradeEntity trade = tradeService.getById(exitStrategyId).orElseThrow(() -> new IllegalStateException(NAME + ": strategy with id: " + exitStrategyId + " was not found!"));
            Optional<TrailingStopExitStrategyProperties> properties = propertiesService.loadProperties(TrailingStopExitStrategyProperties.class);
            BigDecimal distancePercentage = properties.map(TrailingStopExitStrategyProperties::getDistance).orElse(BigDecimal.valueOf(0.01));

            TradingStrategy.TradingSignal tradeType = trade.getEntryOrder().getTradingSignal();

            Timeframe initialTrailingValuesTimeframe = new Timeframe(timeframe.size(), List.of(
                    new Tick(
                            trade.getEntryOrder().getTime(),
                            getDistanceFromPrice(distancePercentage, trade.getEntryOrder().getPrice(), tradeType)
                    )
            ));
            Tick previousTicker = timeframe.getTicks().getFirst();
            Timeframe trailingStopValues = timeframe.getTicks().stream()
                    .reduce(
                            // new timeframe to keep track of the trailing values
                            initialTrailingValuesTimeframe,
                            // calculate trailing value for each tick and put it in the wireframe
                            (trailingStopTimeframe, ticker) -> {
                                // the timeframe is initialized with the first value so ignore it
                                if (previousTicker.equals(ticker)) {
                                    return trailingStopTimeframe;
                                }

                                BigDecimal previousTrailingStopValue = trailingStopTimeframe.getTicks().getLast().getValue();
                                BigDecimal currentTrailingStopValue = getCurrentTrailingPriceUpdate(distancePercentage, previousTrailingStopValue, ticker.getValue(), tradeType)
                                        .orElse(previousTrailingStopValue);

                                trailingStopTimeframe.addTick(new Tick(ticker.getTime(), currentTrailingStopValue));
                                return trailingStopTimeframe;
                            },
                            // function to merge the new timeframes in case we run in parallel
                            (a, b) -> new Timeframe(timeframe.size(), Stream.concat(a.getTicks().stream(), b.getTicks().stream()).collect(Collectors.toList())));

            boolean isCrossover = timeframe.crossover(trailingStopValues) != 0;

            return Optional.ofNullable(isCrossover ? getOrderTypeExitSignal(tradeType) : null);
        };
    }

    /**
     * Checks if the trailing stop value need updating and return the new value if it does.
     * The trailing stop value needs updating if the price moved to the direction we want it to move and the current trailing stop distance surpassed the previous one.
     * For example, if we have a buy order and the price goes up we want the trailing stop value to go up as well based on the current price.
     * However, if the price moved to the opposite direction the trailing stop does not update.
     */
    private Optional<BigDecimal> getCurrentTrailingPriceUpdate(BigDecimal distancePercentage, BigDecimal previousTrailingStopValue, BigDecimal currentValue, TradingStrategy.TradingSignal orderType) {
        BigDecimal distanceFromPrice = getDistanceFromPrice(distancePercentage, currentValue, orderType);
        if (TradingStrategy.TradingSignal.BUY.equals(orderType)) {
            return distanceFromPrice.compareTo(previousTrailingStopValue) > 0 ? Optional.of(distanceFromPrice) : Optional.empty();
        }

        if (TradingStrategy.TradingSignal.SELL.equals(orderType)) {
            return distanceFromPrice.compareTo(previousTrailingStopValue) < 0 ? Optional.of(distanceFromPrice) : Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Returns the distance price from the specified price based on the order type we specified.
     * If we have a buy order the trailing stop distance will be below/less and if we have a sell order the trailing
     * distance will be above the current price.
     */
    private BigDecimal getDistanceFromPrice(BigDecimal distancePercentage, BigDecimal price, TradingStrategy.TradingSignal orderType) {
        return TradingStrategy.TradingSignal.BUY.equals(orderType) ? price.subtract(distancePercentage.multiply(price)) : price.add(distancePercentage.multiply(price));
    }

    /**
     * Returns the appropriate trading signal for the passed in orderType. Basically the opposite operation.
     * If we have an open BUY trade then sell and vice versa.
     */
    private TradingStrategy.TradingSignal getOrderTypeExitSignal(TradingStrategy.TradingSignal tradeType) {
        return TradingStrategy.TradingSignal.BUY.equals(tradeType) ? TradingStrategy.TradingSignal.SELL : TradingStrategy.TradingSignal.BUY;
    }
}
