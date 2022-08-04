import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import database.DatabaseModule;
import entities.TradeTransaction;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import httpclients.kraken.response.trades.TradeDetails;
import httpclients.kraken.response.trades.TradesResponse;
import lombok.extern.slf4j.Slf4j;
import services.BannerService;
import services.strategies.TradingStrategiesModule;
import services.strategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class MoneyMakerApplication {

    public static void main(String[] args) {

        int timeframeSize = 200;
        String assetCode = "XBTGBP";
        String assetDetailCode = "XXBTZGBP";

        new BannerService().printBanner();

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule(), new TradingStrategiesModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
        TradeService tradeService = injector.getInstance(TradeService.class);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // initialize each strategy
        loadServices(TradingStrategy.class, injector).stream()
                .filter(TradingStrategy::enabled)
                .forEach(strategy -> {
                    Timeframe timeframe = new Timeframe(timeframeSize);

                    // initialize timeframe with previous trades
                    Optional<TradesResponse> tradesResponse = krakenClient.getHistoricData(assetCode, strategy.periodLength());
                    tradesResponse.ifPresent(response -> {
                        List<TradeDetails> tradeDetails = response.getResult().getTradeDetails(assetDetailCode);
                        tradeDetails.stream().sorted(Comparator.comparing(TradeDetails::getTime))
                                .collect(Collectors.toList())
                                .forEach(detail -> {
                                    Tick ticker = new Tick(detail.getTime(), detail.getPrice());
                                    timeframe.addTick(ticker);
                                });
                    });
                    log.info(strategy.name() + ": Initialized trading timeframe.");

                    scheduler.scheduleAtFixedRate(() -> krakenClient.getTickerInfo(assetCode).ifPresent(tickerPairResponse -> {
                        BigDecimal currentPrice = tickerPairResponse.getResult().get(assetDetailCode).getCurrentPrice();
                        boolean isPriceChanged = timeframe.getTicks().isEmpty() || !timeframe.getTicks().getLast().getValue().equals(currentPrice);
                        if (isPriceChanged) {
                            timeframe.addTick(currentPrice);

                            if (timeframe.isFull()) {
                                strategy.strategy().apply(timeframe)
                                        .ifPresent(signal -> {
                                            TradeTransaction tradeTransaction = new TradeTransaction();
                                            tradeTransaction.setType(signal);
                                            tradeTransaction.setPrice(currentPrice);
                                            tradeTransaction.setStrategy(strategy.name());
                                            tradeTransaction.setTime(LocalDateTime.now());
                                            tradeTransaction.setAssetCode(assetCode);
                                            tradeTransaction.setCost(BigDecimal.ZERO);
                                            tradeService.trade(tradeTransaction);
                                        });
                            }
                        }
                    }), 2, strategy.periodLength().getSeconds(), TimeUnit.SECONDS);
                    log.info(strategy.name() + ": Trading session started! Looking for a good ticker to trade: " + assetCode);
                });
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
