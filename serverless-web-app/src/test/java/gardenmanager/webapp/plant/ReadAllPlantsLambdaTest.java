package gardenmanager.webapp.plant;

import java.util.Arrays;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.MockCognito;
import gardenmanager.webapp.util.MockContext;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;

@UseTables({"GARDENER_local", "SPECIES_local", "PLANT_local"})
public class ReadAllPlantsLambdaTest {
    @InjectDynamo
    private DynamoDbClient dynamo;
    private DependencyFactory deps;
    private ReadAllPlantsLambda lambda;
    private String email;

    @BeforeEach
    void setup() {
        deps = new DependencyFactory(dynamo);
        lambda = new ReadAllPlantsLambda(deps.plantComp(), deps.gardenerComp());
    }

    @Test
    void readAllPlantsForCurrentUserOnly() throws Exception {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");
        final Gardener otherGardener = deps.gardenerComp().findOrCreateGardener("foo2@example.com");

        final Species species1 = deps.plantFactory().createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        final Species species2 = deps.plantFactory().createSpecies(gardener.getId(), "Garden 2", "Garden 3");
        deps.plantFactory().createSpecies(otherGardener.getId(), "Garden 4", "Garden 5");

        APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadAllPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadAllPlantsLambda.Response.class);
        assertThat(response.getResults(), containsInAnyOrder(
                allOf(hasSpecies(species1), hasPlants("Garden 1", "Garden 2")),
                allOf(hasSpecies(species2), hasPlants("Garden 2", "Garden 3"))));
    }

    private APIGatewayProxyResponseEvent execute(final String email) {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        return lambda.handleRequest(input, new MockContext());
    }

    private static Matcher<SpeciesWithPlants> hasSpecies(final Species species) {
        return hasProperty("species", allOf(
                hasProperty("id", equalTo(species.getId())),
                hasProperty("gardenerId", equalTo(species.getGardenerId())),
                hasProperty("name", equalTo(species.getName()))));
    }

    private static Matcher<SpeciesWithPlants> hasPlants(final String... gardenNames) {
        return hasProperty("plants", containsInAnyOrder(
                Arrays.stream(gardenNames)
                        .map(name -> hasProperty("garden", equalTo(name)))
                        .collect(toList())));
    }

    @Test
    void gardenerHasNoPlants() throws Exception {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");

        deps.plantFactory().createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadAllPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadAllPlantsLambda.Response.class);
        assertThat(response.getResults(), hasSize(0));
    }

    @Test
    void emptyDatabase() throws Exception {
        APIGatewayProxyResponseEvent responseEvent = execute("foo@example.com");
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadAllPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadAllPlantsLambda.Response.class);
        assertThat(response.getResults(), hasSize(0));
    }
}
