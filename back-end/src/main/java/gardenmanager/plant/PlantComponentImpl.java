package gardenmanager.plant;

import java.util.List;

import gardenmanager.domain.Garden;
import gardenmanager.domain.Plant;

import static java.util.stream.Collectors.toList;

public class PlantComponentImpl implements PlantComponent {
    private final PlantRepository gardens;

    public PlantComponentImpl(final PlantRepository gardens) {
        this.gardens = gardens;
    }

    @Override
    public List<Garden> findGardensByGardenerId(final String gardenerId) {
        return gardens.findAllByGardenerId(gardenerId).stream()
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
}
