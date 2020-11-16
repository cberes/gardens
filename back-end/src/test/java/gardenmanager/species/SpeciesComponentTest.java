package gardenmanager.species;

import java.util.Optional;

import gardenmanager.domain.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpeciesComponentTest {
    private SpeciesRepository repo;
    private SpeciesComponent comp;

    @BeforeEach
    void setup() {
        repo = InMemorySpeciesRepository.empty();
        comp = new SpeciesComponentImpl(repo);
    }

    @Test
    void testSaveWithNullGardenerId() {
        final Species species = new Species();
        species.setGardenerId(null);
        assertThrows(IllegalArgumentException.class, () -> comp.save(species));
    }

    @Test
    void testSaveWithEmptyGardenerId() {
        final Species species = new Species();
        species.setGardenerId("");
        assertThrows(IllegalArgumentException.class, () -> comp.save(species));
    }

    @Test
    void testSaveThenFindById() {
        final Species species = new Species();
        species.setGardenerId("foo");
        species.setName("Example");

        comp.save(species);
        assertThat(species.getId(), notNullValue());

        Optional<Species> found = comp.findSpeciesById(species.getId());
        assertThat(found, hasProperty("present", equalTo(true)));
        assertThat(found.get().getId(), is(species.getId()));
    }
}
