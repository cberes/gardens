package gardenmanager.test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import gardenmanager.common.Repository;

import static java.util.stream.IntStream.range;

public class InMemoryRepository<V, K> implements Repository<V, K> {
    private final Function<Integer, K> idGenerator;
    private final BiConsumer<V, K> idSetter;
    private final Map<K, V> elements;

    public InMemoryRepository(final Function<Integer, K> idGenerator,
                              final BiConsumer<V, K> idSetter,
                              final List<V> elements) {
        this(idGenerator, idSetter, new LinkedHashMap<>());
        range(0, elements.size()).forEach(i -> {
            idSetter.accept(elements.get(i), idGenerator.apply(i));
            this.elements.put(idGenerator.apply(i), elements.get(i));
        });
    }

    public InMemoryRepository(final Function<Integer, K> idGenerator,
                              final BiConsumer<V, K> idSetter,
                              final Map<K, V> elements) {
        this.idGenerator = idGenerator;
        this.idSetter = idSetter;
        this.elements = elements;
    }

    @Override
    public List<V> findAll() {
        return new ArrayList<>(elements.values());
    }

    @Override
    public Optional<V> findById(final K id) {
        return Optional.ofNullable(elements.get(id));
    }

    @Override
    public K save(V value) {
        // yeah this won't work if elements are removed
        final K key = idGenerator.apply(elements.size());
        idSetter.accept(value, key);
        elements.put(key, value);
        return key;
    }

    @Override
    public void delete(final V value) {
        // TODO need ID getter to delete the correct value
    }
}
