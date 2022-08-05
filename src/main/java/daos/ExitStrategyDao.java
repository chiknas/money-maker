package daos;

import com.google.inject.Inject;
import entities.ExitStrategyEntity;
import jakarta.persistence.EntityManager;

public class ExitStrategyDao extends AbstractDao<ExitStrategyEntity> {
    @Inject
    public ExitStrategyDao(EntityManager entityManager) {
        super(entityManager);
    }
}
