package services.strategies;

import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
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

    String name();

    Duration periodLength();

    Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy();
}
