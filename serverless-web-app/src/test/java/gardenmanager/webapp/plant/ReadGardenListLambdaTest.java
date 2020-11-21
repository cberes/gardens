package gardenmanager.webapp.plant;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@UseTables({"GARDENER_local", "SPECIES_local", "PLANT_local"})
public class ReadGardenListLambdaTest {
    @InjectDynamo
    private DynamoDbClient dynamo;
    private DependencyFactory deps;
    private ReadGardenListLambda lambda;

    @BeforeEach
    void setup() {
        deps = new DependencyFactory(dynamo);
        lambda = new ReadGardenListLambda(deps.plantComp(), deps.gardenerComp());
    }

    @Test
    void getsUniqueListOfGardensForCurrentUserOnly() throws Exception {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");
        final Gardener otherGardener = deps.gardenerComp().findOrCreateGardener("foo2@example.com");

        deps.plantFactory().createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        deps.plantFactory().createSpecies(gardener.getId(), "Garden 2", "Garden 3");
        deps.plantFactory().createSpecies(otherGardener.getId(), "Garden 4", "Garden 5");

        final APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail());
        assertThat(responseEvent.getStatusCode(), is(200));

        final ReadGardenListLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadGardenListLambda.Response.class);
        assertThat(response.getGardens(), containsInAnyOrder(
                hasProperty("name", equalTo("Garden 1")),
                hasProperty("name", equalTo("Garden 2")),
                hasProperty("name", equalTo("Garden 3"))));
    }

    private APIGatewayProxyResponseEvent execute(final String email) {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        return lambda.handleRequest(input, new MockContext());
    }

    @Test
    void gardenerHasNoPlants() throws Exception {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");

        deps.plantFactory().createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        final APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail());
        assertThat(responseEvent.getStatusCode(), is(200));

        final ReadGardenListLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadGardenListLambda.Response.class);
        assertThat(response.getGardens(), hasSize(0));
    }

    @Test
    void emptyDatabase() throws Exception {
        final APIGatewayProxyResponseEvent responseEvent = execute("foo@example.com");
        assertThat(responseEvent.getStatusCode(), is(200));

        final ReadGardenListLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadGardenListLambda.Response.class);
        assertThat(response.getGardens(), hasSize(0));
    }
}
