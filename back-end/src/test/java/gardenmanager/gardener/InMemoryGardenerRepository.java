package gardenmanager.gardener;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Gardener;
import gardenmanager.test.InMemoryRepository;

import static java.util.Collections.emptyList;

public class InMemoryGardenerRepository extends InMemoryRepository<Gardener, String> implements GardenerRepository {
    public InMemoryGardenerRepository(final List<Gardener> gardeners) {
        super(Object::toString, Gardener::setId, gardeners);
    }

    @Override
    public Optional<Gardener> findByEmail(final String email) {
        return findAll().stream()
                .filter(it -> it.getEmail().equals(email))
                .findAny();
    }

    public static InMemoryGardenerRepository empty() {
        return new InMemoryGardenerRepository(emptyList());
    }
}
