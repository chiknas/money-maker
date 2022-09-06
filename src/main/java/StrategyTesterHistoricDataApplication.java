import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import database.entities.TradeEntity;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import database.entities.TradeOrderType;
import lombok.extern.slf4j.Slf4j;
import services.TimeService;
import services.httpclients.HttpClientModule;
import services.httpclients.kraken.KrakenClient;
import services.httpclients.kraken.KrakenModule;
import services.strategies.TradingStrategiesModule;
import services.strategies.exitstrategies.ExitStrategy;
import services.strategies.exitstrategies.TrailingStopExitStrategy;
import services.strategies.tradingstrategies.ThreeEmaCrossoverStrategy;
import services.strategies.tradingstrategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Dataset downloaded from the internet will have to be converted to be 2 columns (csv) where
 * 1st column is the time in seconds and 2nd is the price.
 * They wil also need to be sorted from oldest to newest.s
 * Historic data found below:
 * <a href="https://www.cryptodatadownload.com/data/bitstamp/">Crypto historic data</a>
 */
@Slf4j
public class StrategyTesterHistoricDataApplication {

    // csv file in the resources folder. the structure of the csv file should be 1st column time in seconds and 2nd column price.
    static final String mockDataFilename = "Bitstamp_BTCGBP_1h.csv";

    public static void main(String[] args) {
        int timeframeSize = 250;
        String assetCode = "XBTGBP";

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule(), new TradingStrategiesModule());
        TradeService tradeService = injector.getInstance(TradeService.class);

        // select a strategy here
        TradingStrategy strategy = injector.getInstance(ThreeEmaCrossoverStrategy.class);

        // select exit strategy here
        ExitStrategy exitStrategy = injector.getInstance(TrailingStopExitStrategy.class);

        Timeframe timeframe = new Timeframe(timeframeSize);

        getHistoricDataReader(timeframeSize).ifPresent(br -> {
            String line;
            try {
                line = br.readLine();
                int initializationIndex = 0;
                // initialize timeframe with previous trades
                while (line != null && initializationIndex < timeframeSize) {
                    String[] split = line.split(",");
                    double currentTime = Double.parseDouble(split[0]);
                    BigDecimal currentPrice = new BigDecimal(split[1]);
                    Tick currentTick = new Tick(TimeService.getLocalDateTimeSecond(currentTime), currentPrice);
                    timeframe.addTick(currentTick);
                    line = br.readLine();
                    initializationIndex++;
                }
                log.info(strategy.name() + ": Initialized trading timeframe.");

                // start the algorithm
                while (line != null) {
                    String[] split = line.split(",");
                    double currentTime = Double.parseDouble(split[0]);
                    BigDecimal currentPrice = new BigDecimal(split[1]);
                    Tick currentTick = new Tick(TimeService.getLocalDateTimeSecond(currentTime), currentPrice);
                    timeframe.addTick(currentTick);
                    log.info(initializationIndex + " - " + currentTick.getTime());

                    // check open trades and close if exit strategy says so
                    tradeService.getOpenTradesByStrategy(strategy.name()).forEach(trade -> {
                        exitStrategy.strategy().apply(trade.getId(), timeframe).ifPresent(closeTradeSignal -> {
                            log.info("Exiting the following order with these details!!");
                            log.info("    TradeId: " + trade.getId());
                            log.info("    ExitSignal: " + closeTradeSignal);
                            log.info("    ExitPrice: " + currentTick.getValue());
                            log.info("    ExitTime: " + currentTick.getTime().toString());

                            TradeOrderEntity exitOrder = new TradeOrderEntity();
                            exitOrder.setOrderReference(1234);
                            exitOrder.setOrderTransaction("qwe");
                            exitOrder.setType(TradeOrderType.EXIT);
                            exitOrder.setTradingSignal(closeTradeSignal);
                            exitOrder.setPrice(currentTick.getValue());
                            exitOrder.setVolume(BigDecimal.TEN);
                            exitOrder.setTime(currentTick.getTime());
                            exitOrder.setStatus(TradeOrderStatus.EXECUTED);
                            exitOrder.setAssetCode(assetCode);
                            exitOrder.setCost(BigDecimal.ZERO);

                            trade.addOrder(exitOrder);

                            BigDecimal margin = trade.getEntryOrder().getTradingSignal().equals(TradingStrategy.TradingSignal.BUY)
                                    ? currentTick.getValue().subtract(trade.getEntryOrder().getPrice())
                                    : trade.getEntryOrder().getPrice().subtract(currentTick.getValue());
                            BigDecimal divisor = currentTick.getValue().add(trade.getEntryOrder().getPrice()).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_EVEN);
                            trade.setProfit(margin.divide(divisor, 10, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100)));

                            tradeService.save(trade);
                        });
                    });


                    strategy.strategy().apply(timeframe)
                            // react to the specified trading signal
                            .ifPresent(signal -> {

                                TradeOrderEntity entryOrder = new TradeOrderEntity();
                                entryOrder.setOrderReference(1234);
                                entryOrder.setOrderTransaction("qwe");
                                entryOrder.setType(TradeOrderType.ENTRY);
                                entryOrder.setTradingSignal(signal);
                                entryOrder.setPrice(currentTick.getValue());
                                entryOrder.setStatus(TradeOrderStatus.EXECUTED);
                                entryOrder.setVolume(BigDecimal.TEN);
                                entryOrder.setTime(currentTick.getTime());
                                entryOrder.setAssetCode(assetCode);
                                entryOrder.setCost(BigDecimal.ZERO);

                                TradeEntity trade = new TradeEntity();
                                trade.setEntryStrategy(strategy.name());
                                trade.setExitStrategy(exitStrategy.name());
                                trade.setPeriodLength(strategy.periodLength());
                                trade.addOrder(entryOrder);

                                tradeService.save(trade);
                            });

                    line = br.readLine();
                    initializationIndex++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public static Optional<BufferedReader> getHistoricDataReader(int timeframeSize) {
        return Optional.ofNullable(KrakenClient.class.getClassLoader().getResourceAsStream(mockDataFilename)).map(resourceAsStream -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
            try {
                int index = 0;
                while (index < timeframeSize) {
                    br.readLine();
                    index++;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return br;
        });
    }
}
