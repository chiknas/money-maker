package daos;

import com.google.inject.Inject;
import entities.TradeEntity;
import jakarta.persistence.EntityManager;

public class TradeDao extends AbstractDao<TradeEntity> {
    @Inject
    public TradeDao(EntityManager entityManager) {
        super(entityManager);
    }
}
