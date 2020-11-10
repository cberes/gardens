package gardenmanager.species;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Species;

public interface SpeciesComponent {
    Optional<Species> findSpeciesById(String id);

    List<Species> findSpeciesByGardenerId(String gardenerId);

    String save(Species species);

    void delete(Species species);
}
