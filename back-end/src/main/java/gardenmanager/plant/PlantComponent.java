package gardenmanager.plant;

import java.util.List;

import gardenmanager.domain.Garden;

public interface PlantComponent {
    List<Garden> findGardensByGardenerId(String gardenerId);
}
