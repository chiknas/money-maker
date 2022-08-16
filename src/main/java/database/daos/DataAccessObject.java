package database.daos;

import java.math.BigInteger;
import java.util.Optional;

public interface DataAccessObject<T> {

    void save(T value);

    Optional<T> findById(BigInteger id);
}
