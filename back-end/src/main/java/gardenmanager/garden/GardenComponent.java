package gardenmanager.garden;

import java.util.List;

import gardenmanager.domain.Garden;

public interface GardenComponent {
    List<Garden> findGardensByGardenerId(String gardenerId);
}
