package gardenmanager.common;

import java.util.List;
import java.util.Optional;

public interface Repository<V, K> {
    List<V> findAll();

    Optional<V> findById(K id);

    K save(V value);

    void delete(V value);
}
