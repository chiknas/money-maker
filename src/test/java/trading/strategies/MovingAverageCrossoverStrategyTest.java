package trading.strategies;

import org.junit.jupiter.api.Test;
import trading.strategies.TradingStrategy.TradingSignal;
import trading.timeframe.Tick;
import trading.timeframe.Timeframe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovingAverageCrossoverStrategyTest {

    @Test
    void sellSignal() {
        // time frame that represent a sudden drop on the price which means the price is about to go down so SELL
        Timeframe<BigDecimal> sellSignalTimeFrame = new Timeframe<>(10);
        LocalDateTime startTime = LocalDateTime.now();
        sellSignalTimeFrame.addTick(new Tick<>(startTime, BigDecimal.valueOf(2)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(10), BigDecimal.valueOf(4)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(20), BigDecimal.valueOf(6)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(30), BigDecimal.valueOf(8)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(40), BigDecimal.valueOf(10)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(50), BigDecimal.valueOf(12)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(60), BigDecimal.valueOf(14)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(70), BigDecimal.valueOf(16)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(80), BigDecimal.valueOf(18)));
        sellSignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(90), BigDecimal.valueOf(5)));

        Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy = new MovingAverageCrossoverStrategy(2, 5).strategy();
        Optional<TradingSignal> buySignal = strategy.apply(sellSignalTimeFrame);

        assertTrue(buySignal.isPresent());
        assertEquals(TradingSignal.SELL, buySignal.get());
    }

    @Test
    void buySignal() {
        // time frame that represent a sudden rise on the price which means the price is about to go up so BUY
        Timeframe<BigDecimal> buySignalTimeFrame = new Timeframe<>(10);
        LocalDateTime startTime = LocalDateTime.now();
        buySignalTimeFrame.addTick(new Tick<>(startTime, BigDecimal.valueOf(20)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(10), BigDecimal.valueOf(18)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(20), BigDecimal.valueOf(16)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(30), BigDecimal.valueOf(14)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(40), BigDecimal.valueOf(12)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(50), BigDecimal.valueOf(10)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(60), BigDecimal.valueOf(8)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(70), BigDecimal.valueOf(6)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(80), BigDecimal.valueOf(4)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(90), BigDecimal.valueOf(18)));

        Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy = new MovingAverageCrossoverStrategy(2, 5).strategy();
        Optional<TradingSignal> buySignal = strategy.apply(buySignalTimeFrame);

        assertTrue(buySignal.isPresent());
        assertEquals(TradingSignal.BUY, buySignal.get());
    }

    @Test
    void noneSignal() {
        // time frame that represent a stable price which means we dont do anything NONE
        Timeframe<BigDecimal> buySignalTimeFrame = new Timeframe<>(10);
        LocalDateTime startTime = LocalDateTime.now();
        buySignalTimeFrame.addTick(new Tick<>(startTime, BigDecimal.valueOf(20)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(10), BigDecimal.valueOf(18)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(20), BigDecimal.valueOf(16)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(30), BigDecimal.valueOf(14)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(40), BigDecimal.valueOf(12)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(50), BigDecimal.valueOf(10)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(60), BigDecimal.valueOf(8)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(70), BigDecimal.valueOf(6)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(80), BigDecimal.valueOf(4)));
        buySignalTimeFrame.addTick(new Tick<>(startTime.plusSeconds(90), BigDecimal.valueOf(2)));

        Function<Timeframe<BigDecimal>, Optional<TradingSignal>> strategy = new MovingAverageCrossoverStrategy(2, 5).strategy();
        Optional<TradingSignal> buySignal = strategy.apply(buySignalTimeFrame);

        assertTrue(buySignal.isEmpty());
    }
}