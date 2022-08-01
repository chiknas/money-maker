package daos;

import com.google.inject.Inject;
import entities.TradeTransaction;
import jakarta.persistence.EntityManager;

public class TradeTransactionsDao extends AbstractDao<TradeTransaction> {
    @Inject
    public TradeTransactionsDao(EntityManager entityManager) {
        super(entityManager);
    }
}
