package gardenmanager.plant;

import java.util.List;

import gardenmanager.domain.Garden;
import gardenmanager.domain.Plant;

public interface PlantComponent {
    List<Garden> findGardensByGardenerId(String gardenerId);

    List<Plant> findPlantsBySpeciesId(String speciesId);

    String save(Plant plant);

    void delete(Plant plant);
}
