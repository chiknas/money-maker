package services.trades;

import database.daos.TradeDao;
import database.entities.TradeEntity;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import database.entities.TradeOrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import properties.PropertiesService;
import properties.TradeProperties;
import services.httpclients.kraken.KrakenClient;
import services.httpclients.kraken.response.balance.BalanceResponse;
import services.httpclients.kraken.response.balance.BalanceResult;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeServiceTest {

    @Mock
    private TradeDao tradeDao;
    @Mock
    private KrakenClient client;
    @Mock
    private PropertiesService propertiesService;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeDao = mock(TradeDao.class);
        client = mock(KrakenClient.class);
        propertiesService = mock(PropertiesService.class);

        BigDecimal capitalAtRisk = new BigDecimal("0.2");
        TradeProperties tradeProperties = new TradeProperties("XBTGBP", "XXBTZGBP",
                "ZGBP", "XXBT", capitalAtRisk, false, "1:1");
        when(propertiesService.loadProperties(eq(TradeProperties.class))).thenReturn(Optional.of(tradeProperties));

        tradeService = new TradeService(tradeDao, client, propertiesService);
    }

    @Test
    void generateTradeReference() {
        // make sure this is always up to 10 digits to ensure:
        // https://docs.kraken.com/rest/#tag/User-Trading/operation/addOrder
        assertEquals(10, String.valueOf(tradeService.generateTradeReference()).length());
    }

    @Test
    void calculateTradeProfitProfitable() {
        TradeEntity trade = new TradeEntity();

        // the cost will always be the cash we spent or got
        // we paid 19000 to enter the market
        TradeOrderEntity entryOrder = new TradeOrderEntity();
        entryOrder.setCost(new BigDecimal(19000));
        entryOrder.setType(TradeOrderType.ENTRY);
        trade.addOrder(entryOrder);

        // we got 20000 when we were getting out
        TradeOrderEntity exitOrder = new TradeOrderEntity();
        exitOrder.setCost(new BigDecimal(20000));
        exitOrder.setType(TradeOrderType.EXIT);
        exitOrder.setStatus(TradeOrderStatus.EXECUTED);
        trade.addOrder(exitOrder);

        Optional<BigDecimal> profit = tradeService.calculateTradeProfit(trade);
        assertTrue(profit.isPresent());
        assertEquals(new BigDecimal(1000), profit.get());
    }

    @Test
    void calculateTradeProfitLoss() {
        TradeEntity trade = new TradeEntity();

        // the cost will always be the cash we spent or got
        // we paid 20000 to enter the market
        TradeOrderEntity entryOrder = new TradeOrderEntity();
        entryOrder.setCost(new BigDecimal(20000));
        entryOrder.setType(TradeOrderType.ENTRY);
        entryOrder.setStatus(TradeOrderStatus.EXECUTED);
        trade.addOrder(entryOrder);

        // we got 15000 when we were getting out
        TradeOrderEntity exitOrder = new TradeOrderEntity();
        exitOrder.setCost(new BigDecimal(15000));
        exitOrder.setType(TradeOrderType.EXIT);
        exitOrder.setStatus(TradeOrderStatus.EXECUTED);
        trade.addOrder(exitOrder);

        // we lost 5000
        Optional<BigDecimal> profit = tradeService.calculateTradeProfit(trade);
        assertTrue(profit.isPresent());
        assertEquals(new BigDecimal(-5000), profit.get());
    }

    @Test
    void calculateTradeProfitOpenTrade() {
        TradeEntity trade = new TradeEntity();

        // the cost will always be the cash we spent or got
        TradeOrderEntity entryOrder = new TradeOrderEntity();
        entryOrder.setCost(new BigDecimal(20000));
        entryOrder.setType(TradeOrderType.ENTRY);
        trade.addOrder(entryOrder);

        // there no exit order so the trade is still open. profit should be empty
        Optional<BigDecimal> profit = tradeService.calculateTradeProfit(trade);
        assertTrue(profit.isEmpty());
    }

    @Test
    void getBuyCryptoVolume() {
        BalanceResponse balanceResponse = mock(BalanceResponse.class);
        BalanceResult balanceResult = mock(BalanceResult.class);
        when(balanceResult.getAssetBalance(eq("ZGBP"))).thenReturn(new BigDecimal("10"));
        when(balanceResponse.getResult()).thenReturn(balanceResult);

        BigDecimal sellCryptoVolume = tradeService.getBuyCryptoVolume(balanceResponse, new BigDecimal("20000"));
        // the capital at risk it at 20% and we have 10GBP so the available balance is:
        //    10 * 0.2 = 2
        // then convert to BTC volume using the price:
        //    available balance / price = 2 / 20000 = 0.0001
        assertEquals(new BigDecimal("0.0001"), sellCryptoVolume.stripTrailingZeros());
    }

    @Test
    void getSellCryptoVolume() {
        BalanceResponse balanceResponse = mock(BalanceResponse.class);
        BalanceResult balanceResult = mock(BalanceResult.class);
        when(balanceResult.getAssetBalance(eq("XXBT"))).thenReturn(new BigDecimal("0.0005"));
        when(balanceResponse.getResult()).thenReturn(balanceResult);

        BigDecimal sellCryptoVolume = tradeService.getSellCryptoVolume(balanceResponse);
        // the capital at risk it at 20% and we have 0.0005 btc so the result should be:
        // 0.0005 * 0.2 = 0.0001
        assertEquals(new BigDecimal("0.0001"), sellCryptoVolume.stripTrailingZeros());
    }
}