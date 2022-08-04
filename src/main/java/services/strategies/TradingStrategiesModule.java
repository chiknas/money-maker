package services.strategies;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class TradingStrategiesModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();
        Multibinder<TradingStrategy> tradingStrategyBinder = Multibinder.newSetBinder(binder(), TradingStrategy.class);
        tradingStrategyBinder.addBinding().to(ThreeEmaCrossoverStrategy.class);
        tradingStrategyBinder.addBinding().to(GoldenCrossStrategy.class);
    }
}
