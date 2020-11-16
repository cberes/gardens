package gardenmanager.plant;

import java.util.List;
import java.util.Optional;

import gardenmanager.domain.Garden;
import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.species.InMemorySpeciesRepository;
import gardenmanager.species.SpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlantComponentTest {
    private PlantRepository plantRepo;
    private SpeciesRepository speciesRepo;
    private PlantComponent comp;

    @BeforeEach
    void setup() {
        plantRepo = InMemoryPlantRepository.empty();
        speciesRepo = InMemorySpeciesRepository.empty();
        comp = new PlantComponentImpl(plantRepo, speciesRepo);
    }

    @Test
    void testSaveWithNullGardenerId() {
        final Plant plant = new Plant();
        plant.setGardenerId(null);
        plant.setSpeciesId("1");
        assertThrows(IllegalArgumentException.class, () -> comp.save(plant));
    }

    @Test
    void testSaveWithEmptyGardenerId() {
        final Plant plant = new Plant();
        plant.setGardenerId("");
        plant.setSpeciesId("1");
        assertThrows(IllegalArgumentException.class, () -> comp.save(plant));
    }

    @Test
    void testSaveWithNullSpeciesId() {
        final Plant plant = new Plant();
        plant.setSpeciesId(null);
        plant.setGardenerId("1");
        assertThrows(IllegalArgumentException.class, () -> comp.save(plant));
    }

    @Test
    void testSaveWithEmptySpeciesId() {
        final Plant plant = new Plant();
        plant.setSpeciesId("");
        plant.setGardenerId("1");
        assertThrows(IllegalArgumentException.class, () -> comp.save(plant));
    }

    @Test
    void testSaveThenFindBySpeciesId() {
        final String gardenerId = "foo";

        final Plant plant = createPlant(gardenerId, "Front");

        comp.save(plant);
        assertThat(plant.getId(), notNullValue());

        Optional<SpeciesWithPlants> found = comp.findPlantsBySpeciesId(plant.getSpeciesId());
        assertThat(found, hasProperty("present", equalTo(true)));
        assertThat(found.get().getPlants(), hasSize(1));
        assertThat(found.get().getPlants().get(0).getId(), is(plant.getId()));
    }

    private Plant createPlant(final String gardenerId, final String garden) {
        final Plant plant = new Plant();
        plant.setGardenerId(gardenerId);
        plant.setSpeciesId(createSpecies(gardenerId));
        plant.setGarden(garden);
        return plant;
    }

    private String createSpecies(final String gardenerId) {
        final Species species = new Species();
        species.setGardenerId(gardenerId);
        return speciesRepo.save(species);
    }

    @Test
    void testFindGardens() {
        final String gardenerId = "foo";
        comp.save(createPlant(gardenerId, "Front"));
        comp.save(createPlant(gardenerId, "Front"));
        comp.save(createPlant("bar", "Side"));
        comp.save(createPlant(gardenerId, "Back"));

        List<Garden> found = comp.findGardensByGardenerId(gardenerId);
        assertThat(found, hasSize(2));
        assertThat(found.stream().map(Garden::getName).collect(toList()),
                containsInAnyOrder("Front", "Back"));
        found.forEach(garden -> assertThat(garden.getGardenerId(), is(gardenerId)));
    }

    @Test
    void testFindPlantsByGardenerId() {
        final String gardenerId = "foo";
        comp.save(createPlant(gardenerId, "Front"));
        comp.save(createPlant(gardenerId, "Front"));
        comp.save(createPlant("bar", "Side"));
        comp.save(createPlant(gardenerId, "Back"));

        List<SpeciesWithPlants> found = comp.findPlantsByGardenerId(gardenerId);
        assertThat(found, hasSize(3));
        assertThat(found.stream()
                        .flatMap(it -> it.getPlants().stream())
                        .map(Plant::getGarden)
                        .collect(toList()), containsInAnyOrder("Front", "Front", "Back"));
        found.forEach(it -> assertThat(it.getSpecies().getGardenerId(), is(gardenerId)));
    }
}
