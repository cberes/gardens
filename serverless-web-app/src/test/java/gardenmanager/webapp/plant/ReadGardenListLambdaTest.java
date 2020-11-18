package gardenmanager.webapp.plant;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Garden;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
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

    private Species createSpecies(final String gardenerId, final String... gardens) {
        Species species = DataFactory.species(gardenerId);
        deps.speciesComp().save(species);

        for (String garden : gardens) {
            deps.plantComp().save(DataFactory.plant(gardenerId, species.getId(), garden));
        }
        return species;
    }

    @Test
    void getsUniqueListOfGardensForCurrentUserOnly() throws Exception {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);
        Gardener otherGardener = deps.gardenerComp().findOrCreateGardener("foo2@example.com");

        createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        createSpecies(gardener.getId(), "Garden 2", "Garden 3");
        createSpecies(otherGardener.getId(), "Garden 4", "Garden 5");

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadGardenListLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadGardenListLambda.Response.class);
        assertThat(response.getGardens().stream().map(Garden::getName).collect(toList()),
                containsInAnyOrder("Garden 1", "Garden 2", "Garden 3"));
    }

    @Test
    void gardenerHasNoPlants() throws Exception {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadGardenListLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadGardenListLambda.Response.class);
        assertThat(response.getGardens(), hasSize(0));
    }

    @Test
    void emptyDatabase() throws Exception {
        final String email = "foo@example.com";

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(200));

        ReadGardenListLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), ReadGardenListLambda.Response.class);
        assertThat(response.getGardens(), hasSize(0));
    }
}
