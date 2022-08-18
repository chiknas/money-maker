package services.strategies.exitstrategies;

import database.entities.TradeEntity;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import properties.PropertiesService;
import properties.TrailingStopExitStrategyProperties;
import services.strategies.tradingstrategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrailingStopExitStrategyTest {

    @Mock
    PropertiesService propertiesService;
    @Mock
    TradeService tradeService;

    TrailingStopExitStrategy trailingStopExitStrategy;

    @BeforeEach
    void setUp() {
        tradeService = mock(TradeService.class);
        propertiesService = mock(PropertiesService.class);
        when(propertiesService.loadProperties(eq(TrailingStopExitStrategyProperties.class)))
                .thenReturn(Optional.of(new TrailingStopExitStrategyProperties(BigDecimal.valueOf(0.2))));

        trailingStopExitStrategy = new TrailingStopExitStrategy(propertiesService, tradeService);
    }

    @Test
    void name() {
        assertEquals(TrailingStopExitStrategy.NAME, trailingStopExitStrategy.name());
    }

    @Test
    void strategyCloseLong() {
        Timeframe trailingStopSignalTriggerTimeframe = new Timeframe(2);
        LocalDateTime startTime = LocalDateTime.now();
        trailingStopSignalTriggerTimeframe.addTick(new Tick(startTime, BigDecimal.valueOf(100)));
        // trailing stop is set to 20% so this should trigger a sell.
        trailingStopSignalTriggerTimeframe.addTick(new Tick(startTime.plusSeconds(10), BigDecimal.valueOf(79)));

        TradeOrderEntity tradeOrder = new TradeOrderEntity();
        tradeOrder.setTime(startTime);
        tradeOrder.setPrice(BigDecimal.valueOf(100));
        tradeOrder.setTradingSignal(TradingStrategy.TradingSignal.BUY);
        tradeOrder.setType(TradeOrderType.ENTRY);

        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.addOrder(tradeOrder);
        when(tradeService.getById(eq(BigInteger.valueOf(1)))).thenReturn(Optional.of(tradeEntity));

        Optional<TradingStrategy.TradingSignal> exitSignal = trailingStopExitStrategy.strategy().apply(BigInteger.valueOf(1), trailingStopSignalTriggerTimeframe);

        assertTrue(exitSignal.isPresent());
        assertEquals(TradingStrategy.TradingSignal.SELL, exitSignal.get());
    }

    @Test
    void strategyCloseShort() {
        Timeframe trailingStopSignalTriggerTimeframe = new Timeframe(2);
        LocalDateTime startTime = LocalDateTime.now();
        trailingStopSignalTriggerTimeframe.addTick(new Tick(startTime, BigDecimal.valueOf(70)));
        // trailing stop is set to 20% so this should trigger a buy.
        trailingStopSignalTriggerTimeframe.addTick(new Tick(startTime.plusSeconds(20), BigDecimal.valueOf(85)));

        TradeOrderEntity tradeOrder = new TradeOrderEntity();
        tradeOrder.setTime(startTime);
        tradeOrder.setPrice(BigDecimal.valueOf(70));
        tradeOrder.setTradingSignal(TradingStrategy.TradingSignal.SELL);
        tradeOrder.setType(TradeOrderType.ENTRY);

        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.addOrder(tradeOrder);
        when(tradeService.getById(eq(BigInteger.valueOf(1)))).thenReturn(Optional.of(tradeEntity));

        Optional<TradingStrategy.TradingSignal> exitSignal = trailingStopExitStrategy.strategy().apply(BigInteger.valueOf(1), trailingStopSignalTriggerTimeframe);

        assertTrue(exitSignal.isPresent());
        assertEquals(TradingStrategy.TradingSignal.BUY, exitSignal.get());
    }

    @Test
    void strategyNoSignal() {
        Timeframe timeframe = new Timeframe(2);
        LocalDateTime startTime = LocalDateTime.now();
        timeframe.addTick(new Tick(startTime, BigDecimal.valueOf(70)));
        timeframe.addTick(new Tick(startTime.plusSeconds(20), BigDecimal.valueOf(60)));

        TradeOrderEntity tradeOrder = new TradeOrderEntity();
        tradeOrder.setTime(startTime);
        tradeOrder.setPrice(BigDecimal.valueOf(70));
        tradeOrder.setTradingSignal(TradingStrategy.TradingSignal.SELL);
        tradeOrder.setType(TradeOrderType.ENTRY);

        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.addOrder(tradeOrder);
        when(tradeService.getById(eq(BigInteger.valueOf(1)))).thenReturn(Optional.of(tradeEntity));

        Optional<TradingStrategy.TradingSignal> exitSignal = trailingStopExitStrategy.strategy().apply(BigInteger.valueOf(1), timeframe);

        assertTrue(exitSignal.isEmpty());
    }
}