package gardenmanager.domain;

import java.util.List;

public class SpeciesWithPlants {
    private Species species;
    private List<Plant> plants;

    public SpeciesWithPlants() {
    }

    public SpeciesWithPlants(final Species species, final List<Plant> plants) {
        this.species = species;
        this.plants = plants;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(final Species species) {
        this.species = species;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(final List<Plant> plants) {
        this.plants = plants;
    }
}
