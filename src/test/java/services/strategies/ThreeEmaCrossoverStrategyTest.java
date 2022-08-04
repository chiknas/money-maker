package services.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import properties.PropertiesService;
import properties.ThreeEmaCrossoverStrategyProperties;
import services.indicators.ExponentialMovingAverageIndicator;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThreeEmaCrossoverStrategyTest {

    ThreeEmaCrossoverStrategy threeEmaCrossoverStrategy;

    @Mock
    PropertiesService propertiesService;

    @Mock
    ExponentialMovingAverageIndicator exponentialMovingAverageIndicator;

    private final int shortPeriod = 9;
    private final int mediumPeriod = 21;
    private final int longPeriod = 55;

    @BeforeEach
    void setUp() {
        propertiesService = mock(PropertiesService.class);
        exponentialMovingAverageIndicator = mock(ExponentialMovingAverageIndicator.class);
        when(propertiesService.loadProperties(eq(ThreeEmaCrossoverStrategyProperties.class)))
                .thenReturn(Optional.of(
                        new ThreeEmaCrossoverStrategyProperties("PT1H", shortPeriod, mediumPeriod, longPeriod)
                ));

        threeEmaCrossoverStrategy = new ThreeEmaCrossoverStrategy(propertiesService, exponentialMovingAverageIndicator);
    }

    @Test
    void name() {
        assertEquals("3EmaCrossover", threeEmaCrossoverStrategy.name());
    }

    @Test
    void strategyCruising() {
        Timeframe prices = new Timeframe(2);

        Timeframe shortEma = new Timeframe(2);
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(20)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(shortPeriod))).thenReturn(shortEma);

        Timeframe mediumEma = new Timeframe(2);
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(mediumPeriod))).thenReturn(mediumEma);

        Timeframe longEma = new Timeframe(2);
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(5)));
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(longPeriod))).thenReturn(longEma);

        Optional<TradingStrategy.TradingSignal> tradingSignal = threeEmaCrossoverStrategy.strategy().apply(prices);

        // EMAs are parallel, in the right order but there was no crossover to trigger a singnal. In other words they are cruising,
        // so we should wait.
        assertTrue(tradingSignal.isEmpty());
    }

    @Test
    void strategyShortCrossoverBuy() {
        Timeframe prices = new Timeframe(2);

        Timeframe shortEma = new Timeframe(2);
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(9)));
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(20)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(shortPeriod))).thenReturn(shortEma);

        Timeframe mediumEma = new Timeframe(2);
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(mediumPeriod))).thenReturn(mediumEma);

        Timeframe longEma = new Timeframe(2);
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(5)));
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(longPeriod))).thenReturn(longEma);

        Optional<TradingStrategy.TradingSignal> tradingSignal = threeEmaCrossoverStrategy.strategy().apply(prices);

        // Short ema crossed the medium ema which was already above the long. now they are parallel and in the
        // right order with a crossover. BUY
        assertTrue(tradingSignal.isPresent());
        assertEquals(TradingStrategy.TradingSignal.BUY, tradingSignal.get());
    }

    @Test
    void strategyMediumCrossoverBuy() {
        Timeframe prices = new Timeframe(2);

        Timeframe shortEma = new Timeframe(2);
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(20)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(shortPeriod))).thenReturn(shortEma);

        Timeframe mediumEma = new Timeframe(2);
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(4)));
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(mediumPeriod))).thenReturn(mediumEma);

        Timeframe longEma = new Timeframe(2);
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(5)));
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(longPeriod))).thenReturn(longEma);

        Optional<TradingStrategy.TradingSignal> tradingSignal = threeEmaCrossoverStrategy.strategy().apply(prices);

        // Medium ema crossed the long ema. now they are parallel and in the
        // right order with a crossover. BUY
        assertTrue(tradingSignal.isPresent());
        assertEquals(TradingStrategy.TradingSignal.BUY, tradingSignal.get());
    }

    @Test
    void strategyCrossoverSell() {
        Timeframe prices = new Timeframe(2);

        Timeframe longEma = new Timeframe(2);
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        longEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(20)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(longPeriod))).thenReturn(longEma);

        Timeframe mediumEma = new Timeframe(2);
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        mediumEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(15)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(mediumPeriod))).thenReturn(mediumEma);

        Timeframe shortEma = new Timeframe(2);
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(16)));
        shortEma.addTick(new Tick(LocalDateTime.now(), BigDecimal.valueOf(10)));
        when(exponentialMovingAverageIndicator.apply(eq(prices), eq(shortPeriod))).thenReturn(shortEma);

        Optional<TradingStrategy.TradingSignal> tradingSignal = threeEmaCrossoverStrategy.strategy().apply(prices);

        // Short ema crossed the medium ema going down. now they are parallel and in the
        // right order with a crossover going down. SELL
        assertTrue(tradingSignal.isPresent());
        assertEquals(TradingStrategy.TradingSignal.SELL, tradingSignal.get());
    }
}