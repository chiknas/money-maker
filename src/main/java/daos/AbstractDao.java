package daos;

import jakarta.persistence.EntityManager;

public class AbstractDao<T> implements DataAccessObject<T> {

    private final EntityManager entityManager;

    public AbstractDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(T value) {
        entityManager.getTransaction().begin();
        entityManager.persist(value);
        entityManager.getTransaction().commit();
    }


}
