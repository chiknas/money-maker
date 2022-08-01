package services.trades;

import com.google.inject.Inject;
import daos.TradeTransactionsDao;
import entities.TradeTransaction;
import services.strategies.TradingStrategy;
import valueobjects.timeframe.Tick;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TradeService {

    private final TradeTransactionsDao transactionsDao;

    @Inject
    public TradeService(TradeTransactionsDao transactionsDao) {
        this.transactionsDao = transactionsDao;
    }

    public void trade(String assetCode, Tick<BigDecimal> tick, TradingStrategy.TradingSignal tradingSignal) {
        TradeTransaction tradeTransaction = new TradeTransaction();
        tradeTransaction.setType(tradingSignal);
        tradeTransaction.setPrice(tick.getValue());
        tradeTransaction.setTime(LocalDateTime.now());
        tradeTransaction.setAssetCode(assetCode);
        tradeTransaction.setCost(BigDecimal.ZERO);

        transactionsDao.save(tradeTransaction);
    }
}
