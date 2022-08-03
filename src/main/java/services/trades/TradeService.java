package services.trades;

import com.google.inject.Inject;
import daos.TradeTransactionsDao;
import entities.TradeTransaction;

/**
 * Service responsible to execute and manage trades in the system.
 */
public class TradeService {

    private final TradeTransactionsDao transactionsDao;

    @Inject
    public TradeService(TradeTransactionsDao transactionsDao) {
        this.transactionsDao = transactionsDao;
    }

    public void trade(TradeTransaction tradeTransaction) {
        transactionsDao.save(tradeTransaction);
    }
}
