package services.strategies.tradingstrategies;

import valueobjects.timeframe.Timeframe;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

/**
 * Interface to be implemented by all trading strategies in the system.
 */
public interface TradingStrategy {

    enum TradingSignal {
        BUY, SELL;
    }

    boolean enabled();

    String name();

    String exitStrategyName();

    Duration periodLength();

    Function<Timeframe, Optional<TradingSignal>> strategy();
}
