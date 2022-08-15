package services.strategies.exitstrategies;

import services.strategies.tradingstrategies.TradingStrategy;
import valueobjects.timeframe.Timeframe;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.BiFunction;

public interface ExitStrategy {
    String name();

    BiFunction<BigInteger, Timeframe, Optional<TradingStrategy.TradingSignal>> strategy();
}
