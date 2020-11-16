package gardenmanager.gardener;

import gardenmanager.domain.Gardener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;

public class GardenerComponentTest {
    private GardenerRepository repo;
    private GardenerComponent comp;

    @BeforeEach
    void setup() {
        repo = InMemoryGardenerRepository.empty();
        comp = new GardenerComponentImpl(repo);
    }

    @Test
    void testFindOrCreateCreatesGardener() {
        final String email = "foo@example.com";

        assertThat(comp.findGardenerByEmail(email), hasProperty("present", equalTo(false)));

        Gardener gardener1 = comp.findOrCreateGardener(email);
        assertThat(gardener1.getEmail(), is(email));

        assertThat(comp.findGardenerByEmail(email), hasProperty("present", equalTo(true)));

        Gardener gardener2 = comp.findOrCreateGardener(email);
        assertThat(gardener2.getId(), is(gardener1.getId()));
    }
}
