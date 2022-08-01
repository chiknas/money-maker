package daos;

public interface DataAccessObject<T> {

    void save(T value);
}
