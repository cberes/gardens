package gardenmanager.plant;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gardenmanager.domain.GardenPlant;
import gardenmanager.domain.Plant;
import gardenmanager.garden.GardenRepository;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;

public class PlantComponentImpl implements PlantComponent {
    private final PlantRepository plants;
    private final GardenRepository gardens;

    public PlantComponentImpl(final PlantRepository plants, final GardenRepository gardens) {
        this.plants = plants;
        this.gardens = gardens;
    }

    @Override
    public Optional<Plant> findPlantById(final String id) {
        final Optional<Plant> plantOpt = plants.findById(id);
        plantOpt.map(Plant::getId)
                .map(gardens::findAllByPlantId)
                .ifPresent(plantOpt.get()::setGardens);
        return plantOpt;
    }

    @Override
    public List<Plant> findPlantsByGardenerId(final String gardenerId) {
        final Map<String, List<GardenPlant>> gardenPlants = gardens.findAllByGardenerId(gardenerId)
                .stream().collect(groupingBy(GardenPlant::getPlantId));

        final List<Plant> allPlants = plants.findAllByGardenerId(gardenerId);
        allPlants.forEach(plant -> plant.setGardens(gardenPlants.getOrDefault(plant.getId(), emptyList())));
        return allPlants;
    }

    @Override
    public String save(final Plant plant) {
        requireGardenerId(plant);
        assignGardenerIds(plant);
        deleteRemovedGardens(plant);
        return plants.save(plant);
    }

    private static void requireGardenerId(final Plant plant) {
        if (plant.getGardenerId() == null || plant.getGardenerId().isEmpty()) {
            throw new IllegalArgumentException("gardenerId is required");
        }
    }

    private static void assignGardenerIds(final Plant plant) {
        if (plant.getGardens() != null) {
            for (GardenPlant garden : plant.getGardens()) {
                garden.setGardenerId(plant.getGardenerId());
            }
        }
    }

    private void deleteRemovedGardens(final Plant plant) {
        final Set<String> idsToKeep = plant.getGardens() == null ? emptySet() : plant.getGardens().stream()
                .map(GardenPlant::getId).collect(toSet());
        final List<GardenPlant> databaseGardens = gardens.findAllByPlantId(plant.getId());
        final List<GardenPlant> gardensToRemove = databaseGardens.stream()
                .filter(garden -> !idsToKeep.contains(plant.getId()))
                .collect(toList());
        gardensToRemove.forEach(gardens::delete);
    }

    @Override
    public void delete(final Plant plant) {
        gardens.findAllByPlantId(plant.getId()).forEach(gardens::delete);
        plants.delete(plant);
    }
}
