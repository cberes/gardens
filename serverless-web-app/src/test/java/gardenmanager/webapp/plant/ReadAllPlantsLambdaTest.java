package gardenmanager.webapp.plant;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.MockCognito;
import gardenmanager.webapp.util.MockContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

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

    @BeforeEach
    void setup() {
        deps = new DependencyFactory(dynamo);
        lambda = new ReadAllPlantsLambda(deps.plantComp(), deps.gardenerComp());
    }

    @Test
    void readAllPlantsForCurrentUserOnly() throws Exception {
        final String email = "foo@example.com";
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);
        final Gardener otherGardener = deps.gardenerComp().findOrCreateGardener("foo2@example.com");

        final Species species1 = deps.plantFactory().createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        final Species species2 = deps.plantFactory().createSpecies(gardener.getId(), "Garden 2", "Garden 3");
        deps.plantFactory().createSpecies(otherGardener.getId(), "Garden 4", "Garden 5");

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadAllPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadAllPlantsLambda.Response.class);
        assertThat(response.getResults(), containsInAnyOrder(
                allOf(
                        hasProperty("species", allOf(
                                hasProperty("id", equalTo(species1.getId())),
                                hasProperty("gardenerId", equalTo(species1.getGardenerId())),
                                hasProperty("name", equalTo(species1.getName())))),
                        hasProperty("plants", containsInAnyOrder(
                                hasProperty("garden", equalTo("Garden 1")),
                                hasProperty("garden", equalTo("Garden 2"))))),
                allOf(
                        hasProperty("species", allOf(
                                hasProperty("id", equalTo(species2.getId())),
                                hasProperty("gardenerId", equalTo(species2.getGardenerId())),
                                hasProperty("name", equalTo(species2.getName())))),
                        hasProperty("plants", containsInAnyOrder(
                                hasProperty("garden", equalTo("Garden 2")),
                                hasProperty("garden", equalTo("Garden 3")))))));
    }

    @Test
    void gardenerHasNoPlants() throws Exception {
        final String email = "foo@example.com";
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        deps.plantFactory().createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadAllPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadAllPlantsLambda.Response.class);
        assertThat(response.getResults(), hasSize(0));
    }

    @Test
    void emptyDatabase() throws Exception {
        final String email = "foo@example.com";

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadAllPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadAllPlantsLambda.Response.class);
        assertThat(response.getResults(), hasSize(0));
    }
}
