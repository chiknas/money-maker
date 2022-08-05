package daos;

import com.google.inject.Inject;
import entities.TradeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;

public class TradeDao extends AbstractDao<TradeEntity> {
    @Inject
    public TradeDao(EntityManager entityManager) {
        super(entityManager);
    }

    public List<TradeEntity> findOpenTradesByStrategy(String strategyName) {
        String hql = "FROM trade t WHERE t.entry_strategy= :strategyName";
        Query query = entityManager.createQuery(hql);
        query.setParameter("strategyName", strategyName);
        return (List<TradeEntity>) query.getResultList();
    }
}
