package gardenmanager.webapp.plant;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

@UseTables({"GARDENER_local", "SPECIES_local", "PLANT_local"})
public class ReadPlantsLambdaTest {
    @InjectDynamo
    private DynamoDbClient dynamo;
    private DependencyFactory deps;
    private ReadPlantsLambda lambda;

    @BeforeEach
    void setup() {
        deps = new DependencyFactory(dynamo);
        lambda = new ReadPlantsLambda(deps.plantComp(), deps.gardenerComp());
    }

    @Test
    void readPlantsOfGivenSpecies() throws Exception {
        final String email = "foo@example.com";
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        final Species species = deps.plantFactory().createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        deps.plantFactory().createSpecies(gardener.getId(), "Garden 2", "Garden 3");

        final APIGatewayProxyResponseEvent responseEvent = execute(email, species.getId());
        assertThat(responseEvent.getStatusCode(), is(200));

        final ReadPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadPlantsLambda.Response.class);

        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().getSpecies(), allOf(
                hasProperty("id", equalTo(species.getId())),
                hasProperty("gardenerId", equalTo(species.getGardenerId())),
                hasProperty("name", equalTo(species.getName()))));
        assertThat(response.getResult().getPlants(), containsInAnyOrder(
                hasProperty("garden", equalTo("Garden 1")),
                hasProperty("garden", equalTo("Garden 2"))));
    }

    private APIGatewayProxyResponseEvent execute(final String email, final String speciesId) {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);
        MockParameters.mockPathParam(input, "speciesId", speciesId);

        return lambda.handleRequest(input, new MockContext());
    }

    @Test
    void readSpeciesWithoutPlants() throws Exception {
        final String email = "foo@example.com";
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        final Species species = deps.plantFactory().createSpecies(gardener.getId());

        final APIGatewayProxyResponseEvent responseEvent = execute(email, species.getId());
        assertThat(responseEvent.getStatusCode(), is(200));

        final ReadPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadPlantsLambda.Response.class);

        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().getSpecies(), allOf(
                hasProperty("id", equalTo(species.getId())),
                hasProperty("gardenerId", equalTo(species.getGardenerId())),
                hasProperty("name", equalTo(species.getName()))));
        assertThat(response.getResult().getPlants(), hasSize(0));
    }

    @Test
    void readPlantsFromOtherGardener() {
        final String email = "foo@example.com";
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        final Species species = deps.plantFactory().createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        final APIGatewayProxyResponseEvent responseEvent = execute(email, species.getId());
        assertThat(responseEvent.getStatusCode(), is(404));
    }

    @Test
    void emptyDatabase() {
        final String email = "foo@example.com";

        final APIGatewayProxyResponseEvent responseEvent = execute(email, "dW5rbm93bg==:unknown");
        assertThat(responseEvent.getStatusCode(), is(404));
    }
}
