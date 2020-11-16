package gardenmanager.plant;

import java.util.List;

import gardenmanager.domain.Plant;
import gardenmanager.test.InMemoryRepository;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class InMemoryPlantRepository extends InMemoryRepository<Plant, String> implements PlantRepository {
    public InMemoryPlantRepository(final List<Plant> gardeners) {
        super(Object::toString, Plant::setId, gardeners);
    }

    @Override
    public List<Plant> findAllByGardenerId(final String gardenerId) {
        return findAll().stream()
                .filter(it -> it.getGardenerId().equals(gardenerId))
                .collect(toList());
    }

    @Override
    public List<Plant> findAllBySpeciesId(final String speciesId) {
        return findAll().stream()
                .filter(it -> it.getSpeciesId().equals(speciesId))
                .collect(toList());
    }

    public static InMemoryPlantRepository empty() {
        return new InMemoryPlantRepository(emptyList());
    }
}
