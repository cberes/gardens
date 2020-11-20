package gardenmanager.webapp.species;

import gardenmanager.domain.Species;
import gardenmanager.plant.PlantComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.DataFactory;

public class PlantFactory {
    private final SpeciesComponent species;
    private final PlantComponent plants;

    public PlantFactory(final SpeciesComponent species, final PlantComponent plants) {
        this.species = species;
        this.plants = plants;
    }

    public Species createSpecies(final String gardenerId, final String... gardens) {
        Species dbSpecies = DataFactory.species(gardenerId);
        species.save(dbSpecies);

        for (String garden : gardens) {
            plants.save(DataFactory.plant(gardenerId, dbSpecies.getId(), garden));
        }
        return dbSpecies;
    }
}
