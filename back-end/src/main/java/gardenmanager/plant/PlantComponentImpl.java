package gardenmanager.plant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import gardenmanager.domain.Garden;
import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.species.SpeciesRepository;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class PlantComponentImpl implements PlantComponent {
    private final PlantRepository plants;
    private final SpeciesRepository speciesRepo;

    public PlantComponentImpl(final PlantRepository plants, final SpeciesRepository speciesRepo) {
        this.plants = plants;
        this.speciesRepo = speciesRepo;
    }

    @Override
    public List<Garden> findGardensByGardenerId(final String gardenerId) {
        return plants.findAllByGardenerId(gardenerId).stream()
                .map(PlantComponentImpl::toGarden)
                .distinct()
                .collect(toList());
    }

    private static Garden toGarden(final Plant plant) {
        Garden garden = new Garden();
        garden.setGardenerId(plant.getGardenerId());
        garden.setName(plant.getGarden());
        return garden;
    }

    @Override
    public List<SpeciesWithPlants> findPlantsByGardenerId(final String gardenerId) {
        final List<Species> allSpecies = speciesRepo.findAllByGardenerId(gardenerId);

        final Map<String, List<Plant>> plantsBySpeciesId = plants.findAllByGardenerId(gardenerId)
                .stream().collect(groupingBy(Plant::getSpeciesId));

        return allSpecies.stream()
                .map(species -> new SpeciesWithPlants(species,
                        plantsBySpeciesId.getOrDefault(species.getId(), emptyList())))
                .collect(toList());
    }

    @Override
    public Optional<SpeciesWithPlants> findPlantsBySpeciesId(final String speciesId) {
        return speciesRepo.findById(speciesId).map(species ->
                new SpeciesWithPlants(species, plants.findAllBySpeciesId(speciesId)));
    }

    @Override
    public String save(final Plant plant) {
        requireGardenerId(plant);
        requireSpeciesId(plant);
        return plants.save(plant);
    }

    private static void requireGardenerId(final Plant plant) {
        if (isNullOrEmpty(plant.getGardenerId())) {
            throw new IllegalArgumentException("gardenerId is required");
        }
    }

    private static void requireSpeciesId(final Plant plant) {
        if (isNullOrEmpty(plant.getSpeciesId())) {
            throw new IllegalArgumentException("speciesId is required");
        }
    }

    private static boolean isNullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public void delete(final Plant plant) {
        plants.delete(plant);
    }
}
