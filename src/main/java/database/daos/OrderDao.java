package database.daos;

import com.google.inject.Inject;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;

public class OrderDao extends AbstractDao<TradeOrderEntity> {

    @Inject
    public OrderDao(EntityManager entityManager) {
        super(entityManager);
    }

    public List<TradeOrderEntity> findPendingOrders() {
        String hql = "FROM TradeOrderEntity t WHERE t.status=" + TradeOrderStatus.PENDING;
        Query query = entityManager.createQuery(hql);
        return (List<TradeOrderEntity>) query.getResultList();
    }
}
