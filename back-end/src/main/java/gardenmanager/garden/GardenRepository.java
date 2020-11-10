package gardenmanager.garden;

import java.util.List;

import gardenmanager.common.Repository;
import gardenmanager.domain.GardenPlant;

public interface GardenRepository extends Repository<GardenPlant, String> {
    List<GardenPlant> findAllByGardenerId(String gardenerId);

    List<GardenPlant> findAllByPlantId(String plantId);
}
