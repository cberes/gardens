package gardenmanager.species;

import java.util.List;

import gardenmanager.domain.Species;
import gardenmanager.test.InMemoryRepository;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class InMemorySpeciesRepository extends InMemoryRepository<Species, String> implements SpeciesRepository {
    public InMemorySpeciesRepository(final List<Species> gardeners) {
        super(Object::toString, Species::setId, gardeners);
    }

    @Override
    public List<Species> findAllByGardenerId(final String gardenerId) {
        return findAll().stream()
                .filter(it -> it.getGardenerId().equals(gardenerId))
                .collect(toList());
    }

    public static InMemorySpeciesRepository empty() {
        return new InMemorySpeciesRepository(emptyList());
    }
}
