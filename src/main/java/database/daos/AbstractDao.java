package database.daos;

import jakarta.persistence.EntityManager;

import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.Optional;

public class AbstractDao<T> implements DataAccessObject<T> {

    protected final EntityManager entityManager;

    public AbstractDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(T value) {
        entityManager.getTransaction().begin();
        entityManager.persist(value);
        entityManager.getTransaction().commit();
    }

    @Override
    public Optional<T> findById(BigInteger id) {
        entityManager.getTransaction().begin();
        T result = entityManager.find((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0], id);
        entityManager.getTransaction().commit();
        return Optional.ofNullable(result);
    }


}
