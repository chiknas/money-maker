import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import database.DatabaseModule;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import lombok.extern.slf4j.Slf4j;
import services.BannerService;
import services.httpclients.HttpClientModule;
import services.httpclients.kraken.KrakenClient;
import services.httpclients.kraken.KrakenModule;
import services.httpclients.kraken.response.trades.TradeDetails;
import services.httpclients.kraken.response.trades.TradesResponse;
import services.strategies.TradingStrategiesModule;
import services.strategies.exitstrategies.ExitStrategy;
import services.strategies.tradingstrategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class MoneyMakerApplication {

    public static void main(String[] args) {

        int timeframeSize = 250;
        String assetCode = "XBTGBP";
        String assetDetailCode = "XXBTZGBP";

        new BannerService().printBanner();

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule(), new TradingStrategiesModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
        TradeService tradeService = injector.getInstance(TradeService.class);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Pick up only the trading strategies that are enabled
        List<TradingStrategy> enabledTradingStrategies = loadServices(TradingStrategy.class, injector).stream()
                .filter(TradingStrategy::enabled).collect(Collectors.toList());

        // Pick up available exit strategies
        List<ExitStrategy> exitStrategies = new ArrayList<>(loadServices(ExitStrategy.class, injector));

        // initialize each strategy
        enabledTradingStrategies.forEach(tradingStrategy -> {
            Timeframe timeframe = new Timeframe(timeframeSize);

            ExitStrategy exitStrategy = exitStrategies.stream()
                    .filter(strategy -> tradingStrategy.exitStrategyName().equals(strategy.name())).findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Exit strategy with name " + tradingStrategy.exitStrategyName() + " was not found for trading strategy: " + tradingStrategy.name()
                    ));
            log.info(tradingStrategy.name() + ": Found exit strategy with name " + exitStrategy.name());

            // initialize timeframe with previous trades
            Optional<TradesResponse> tradesResponse = krakenClient.getHistoricData(assetCode, tradingStrategy.periodLength());
            tradesResponse.ifPresent(response -> {
                List<TradeDetails> tradeDetails = response.getResult().getTradeDetails(assetDetailCode);
                tradeDetails.stream().sorted(Comparator.comparing(TradeDetails::getTime))
                        .collect(Collectors.toList())
                        .forEach(detail -> {
                            Tick ticker = new Tick(detail.getTime(), detail.getPrice());
                            timeframe.addTick(ticker);
                        });
            });
            log.info(tradingStrategy.name() + ": Initialized trading timeframe.");

            // scheduler to run the strategy on the specified period
            scheduler.scheduleAtFixedRate(() -> krakenClient.getTickerInfo(assetCode).ifPresent(tickerPairResponse -> {

                BigDecimal currentPrice = tickerPairResponse.getResult().get(assetDetailCode).getCurrentPrice();
                boolean isPriceChanged = timeframe.getTicks().isEmpty() || !timeframe.getTicks().getLast().getValue().equals(currentPrice);

                // only run the strategy on price change. no point running it for the same price twice.
                if (isPriceChanged) {
                    timeframe.addTick(currentPrice);

                    // only run when the timeframe we are interested in is full with prices.
                    if (timeframe.isFull()) {

                        // check open trades and close if exit strategy says so

                        // for each loop of all open trades from this strategy and try to check if they need closing.
                        tradeService.getOpenTradesByStrategy(tradingStrategy.name()).forEach(trade -> {
                            exitStrategy.strategy().apply(trade.getId(), timeframe).ifPresent(closeTradeSignal -> {
                                TradeOrderEntity exitOrder = new TradeOrderEntity();
                                exitOrder.setOrderReference(UUID.randomUUID());
                                exitOrder.setType(closeTradeSignal);
                                exitOrder.setPrice(currentPrice);
                                exitOrder.setVolume(BigDecimal.TEN);
                                exitOrder.setTime(LocalDateTime.now());
                                exitOrder.setStatus(TradeOrderStatus.PENDING);
                                exitOrder.setAssetCode(assetCode);
                                exitOrder.setCost(BigDecimal.ZERO);

                                trade.setExitOrder(exitOrder);

                                tradeService.save(trade);
                            });
                        });

                        // open new trades based on the trading strategy
                        tradingStrategy.strategy().apply(timeframe)
                                // react to the specified trading signal
                                .ifPresent(signal -> tradeService.openTrade(assetCode, signal, tradingStrategy));
                    }
                }
            }), 2, tradingStrategy.periodLength().getSeconds(), TimeUnit.SECONDS);
            log.info(tradingStrategy.name() + ": Trading session started! Looking for a good ticker to trade: " + assetCode);
        });

        if (enabledTradingStrategies.isEmpty()) {
            log.error("No trading strategies found enabled. You can enable strategies in the application.properties. See ya!");
        }
    }

    public static <T> Set<T> loadServices(Class<T> type, Injector injector) {
        final TypeLiteral<Set<T>> lit = setOf(type);
        final Key<Set<T>> key = Key.get(lit);
        return injector.getInstance(key);
    }

    public static <T> TypeLiteral<Set<T>> setOf(Class<T> type) {
        return (TypeLiteral<Set<T>>) TypeLiteral.get(Types.setOf(type));
    }
}
