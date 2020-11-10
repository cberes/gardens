package gardenmanager.species;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;
import gardenmanager.plant.PlantRepository;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;

public class SpeciesComponentImpl implements SpeciesComponent {
    private final SpeciesRepository plants;
    private final PlantRepository gardens;

    public SpeciesComponentImpl(final SpeciesRepository plants, final PlantRepository gardens) {
        this.plants = plants;
        this.gardens = gardens;
    }

    @Override
    public Optional<Species> findPlantById(final String id) {
        final Optional<Species> plantOpt = plants.findById(id);
        plantOpt.map(Species::getId)
                .map(gardens::findAllByPlantId)
                .ifPresent(plantOpt.get()::setGardens);
        return plantOpt;
    }

    @Override
    public List<Species> findPlantsByGardenerId(final String gardenerId) {
        final Map<String, List<Plant>> gardenPlants = gardens.findAllByGardenerId(gardenerId)
                .stream().collect(groupingBy(Plant::getSpeciesId));

        final List<Species> allSpecies = plants.findAllByGardenerId(gardenerId);
        allSpecies.forEach(plant -> plant.setGardens(gardenPlants.getOrDefault(plant.getId(), emptyList())));
        return allSpecies;
    }

    @Override
    public String save(final Species species) {
        requireGardenerId(species);
        assignGardenerIds(species);
        deleteRemovedGardens(species);
        return plants.save(species);
    }

    private static void requireGardenerId(final Species species) {
        if (species.getGardenerId() == null || species.getGardenerId().isEmpty()) {
            throw new IllegalArgumentException("gardenerId is required");
        }
    }

    private static void assignGardenerIds(final Species species) {
        if (species.getGardens() != null) {
            for (Plant garden : species.getGardens()) {
                garden.setGardenerId(species.getGardenerId());
            }
        }
    }

    private void deleteRemovedGardens(final Species species) {
        final Set<String> idsToKeep = species.getGardens() == null ? emptySet() : species.getGardens().stream()
                .map(Plant::getId).collect(toSet());
        final List<Plant> databaseGardens = gardens.findAllByPlantId(species.getId());
        final List<Plant> gardensToRemove = databaseGardens.stream()
                .filter(garden -> !idsToKeep.contains(species.getId()))
                .collect(toList());
        gardensToRemove.forEach(gardens::delete);
    }

    @Override
    public void delete(final Species species) {
        gardens.findAllByPlantId(species.getId()).forEach(gardens::delete);
        plants.delete(species);
    }
}
