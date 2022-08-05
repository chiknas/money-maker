package services.trades;

import com.google.inject.Inject;
import daos.TradeDao;
import entities.TradeEntity;

/**
 * Service responsible to execute and manage trades in the system.
 */
public class TradeService {

    private final TradeDao transactionsDao;

    @Inject
    public TradeService(TradeDao transactionsDao) {
        this.transactionsDao = transactionsDao;
    }

    public void trade(TradeEntity trade) {
        transactionsDao.save(trade);
    }
}
