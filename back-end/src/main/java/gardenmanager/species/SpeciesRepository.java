package gardenmanager.species;

import java.util.List;

import gardenmanager.common.Repository;
import gardenmanager.domain.Species;

public interface SpeciesRepository extends Repository<Species, String> {
    List<Species> findAllByGardenerId(final String gardenerId);
}
