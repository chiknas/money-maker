package database.daos;

import com.google.inject.Inject;
import database.entities.TradeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;

public class TradeDao extends AbstractDao<TradeEntity> {
    @Inject
    public TradeDao(EntityManager entityManager) {
        super(entityManager);
    }

    public List<TradeEntity> findOpenTradesByStrategy(String strategyName) {
        String hql = "FROM TradeEntity t WHERE t.entryStrategy= :strategyName AND t.exitOrder is NULL";
        Query query = entityManager.createQuery(hql);
        query.setParameter("strategyName", strategyName);
        return (List<TradeEntity>) query.getResultList();
    }
}
