package gardenmanager.plant;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Plant;

public interface PlantComponent {
    Optional<Plant> findPlantById(String id);

    List<Plant> findPlantsByGardenerId(String gardenerId);

    String save(Plant plant);

    void delete(Plant plant);
}
