package services.trades;

import com.google.inject.Inject;
import database.daos.TradeDao;
import database.entities.TradeEntity;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

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

    public List<TradeEntity> getOpenTradesByStrategy(String strategyName) {
        return transactionsDao.findOpenTradesByStrategy(strategyName);

    }

    public Optional<TradeEntity> getById(BigInteger id) {
        return transactionsDao.findById(id);
    }
}
