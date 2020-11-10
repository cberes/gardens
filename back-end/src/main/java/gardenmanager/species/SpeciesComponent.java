package gardenmanager.species;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Species;

public interface SpeciesComponent {
    Optional<Species> findPlantById(String id);

    List<Species> findPlantsByGardenerId(String gardenerId);

    String save(Species species);

    void delete(Species species);
}
