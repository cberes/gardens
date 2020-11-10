package gardenmanager.plant;

import java.util.List;

import gardenmanager.domain.Garden;
import gardenmanager.domain.Plant;

import static java.util.stream.Collectors.toList;

public class PlantComponentImpl implements PlantComponent {
    private final PlantRepository plants;

    public PlantComponentImpl(final PlantRepository plants) {
        this.plants = plants;
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
    public List<Plant> findPlantsBySpeciesId(final String speciesId) {
        return plants.findAllBySpeciesId(speciesId);
    }

    @Override
    public String save(final Plant plant) {
        requireGardenerId(plant);
        return plants.save(plant);
    }

    private static void requireGardenerId(final Plant plant) {
        if (plant.getGardenerId() == null || plant.getGardenerId().isEmpty()) {
            throw new IllegalArgumentException("gardenerId is required");
        }
    }

    @Override
    public void delete(final Plant plant) {
        plants.delete(plant);
    }
}
