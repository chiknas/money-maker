package trading.strategies;

import trading.timeframe.Timeframe;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Interface to be implemented by all trading strategies in the system.
 */
public interface TradingStrategy {

    enum TradingSignal {
        BUY, SELL;
    }

    Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy();
}
