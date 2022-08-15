package services.strategies;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import services.strategies.exitstrategies.ExitStrategy;
import services.strategies.exitstrategies.TrailingStopExitStrategy;
import services.strategies.tradingstrategies.GoldenCrossStrategy;
import services.strategies.tradingstrategies.ThreeEmaCrossoverStrategy;
import services.strategies.tradingstrategies.TradingStrategy;

public class TradingStrategiesModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();
        Multibinder<TradingStrategy> tradingStrategyBinder = Multibinder.newSetBinder(binder(), TradingStrategy.class);
        tradingStrategyBinder.addBinding().to(ThreeEmaCrossoverStrategy.class);
        tradingStrategyBinder.addBinding().to(GoldenCrossStrategy.class);

        Multibinder<ExitStrategy> exitStrategyBinder = Multibinder.newSetBinder(binder(), ExitStrategy.class);
        exitStrategyBinder.addBinding().to(TrailingStopExitStrategy.class);
    }
}
