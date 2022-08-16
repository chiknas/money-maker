package services.trades;

import database.daos.TradeDao;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeServiceTest {

    @Mock
    private TradeDao transactionsDao;
    @Mock
    private KrakenClient client;
    @Mock
    private PropertiesService propertiesService;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        transactionsDao = mock(TradeDao.class);
        client = mock(KrakenClient.class);
        propertiesService = mock(PropertiesService.class);

        BigDecimal capitalAtRisk = new BigDecimal("0.2");
        TradeProperties tradeProperties = new TradeProperties("XBTGBP", "XXBTZGBP",
                "ZGBP", "XXBT", capitalAtRisk, false);
        when(propertiesService.loadProperties(eq(TradeProperties.class))).thenReturn(Optional.of(tradeProperties));

        tradeService = new TradeService(transactionsDao, client, propertiesService);
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