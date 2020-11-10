package gardenmanager.species;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Species;

public class SpeciesComponentImpl implements SpeciesComponent {
    private final SpeciesRepository speciesRepo;

    public SpeciesComponentImpl(final SpeciesRepository speciesRepo) {
        this.speciesRepo = speciesRepo;
    }

    @Override
    public Optional<Species> findSpeciesById(final String id) {
        return speciesRepo.findById(id);
    }

    @Override
    public List<Species> findSpeciesByGardenerId(final String gardenerId) {
        return speciesRepo.findAllByGardenerId(gardenerId);
    }

    @Override
    public String save(final Species species) {
        requireGardenerId(species);
        return speciesRepo.save(species);
    }

    private static void requireGardenerId(final Species species) {
        if (species.getGardenerId() == null || species.getGardenerId().isEmpty()) {
            throw new IllegalArgumentException("gardenerId is required");
        }
    }

    @Override
    public void delete(final Species species) {
        speciesRepo.delete(species);
    }
}
