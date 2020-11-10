package gardenmanager.plant;

import java.util.List;

import gardenmanager.common.Repository;
import gardenmanager.domain.Plant;

public interface PlantRepository extends Repository<Plant, String> {
    List<Plant> findAllByGardenerId(String gardenerId);

    List<Plant> findAllBySpeciesId(String speciesId);
}
