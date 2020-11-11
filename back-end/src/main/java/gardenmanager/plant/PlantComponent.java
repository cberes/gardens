package gardenmanager.plant;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Garden;
import gardenmanager.domain.Plant;
import gardenmanager.domain.SpeciesWithPlants;

public interface PlantComponent {
    List<Garden> findGardensByGardenerId(String gardenerId);

    Optional<SpeciesWithPlants> findPlantsBySpeciesId(String speciesId);

    List<SpeciesWithPlants> findPlantsByGardenerId(String gardenerId);

    String save(Plant plant);

    void delete(Plant plant);
}
