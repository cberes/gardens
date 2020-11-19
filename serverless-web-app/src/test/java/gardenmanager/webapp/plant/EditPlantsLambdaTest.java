package gardenmanager.webapp.plant;

import java.util.List;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.DataFactory;
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

@UseTables({"GARDENER_local", "SPECIES_local", "PLANT_local"})
public class EditPlantsLambdaTest {
    @InjectDynamo
    private DynamoDbClient dynamo;
    private DependencyFactory deps;
    private EditPlantsLambda lambda;

    @BeforeEach
    void setup() {
        deps = new DependencyFactory(dynamo);
        lambda = new EditPlantsLambda(JsonUtils.jackson(), deps.speciesComp(), deps.plantComp(), deps.gardenerComp());
    }

    @Test
    void createPlantsAndGardener() throws Exception {
        final String email = "foo@example.com";
//        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        final EditPlantsLambda.Request request = new EditPlantsLambda.Request();
        request.setSpecies(DataFactory.species(null));
        request.setPlants(List.of(
                DataFactory.plant(null, null, "Garden 1"),
                DataFactory.plant(null, null, "Garden 2")));
        input.setBody(JsonUtils.toJson(request));

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(201));

        EditPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), EditPlantsLambda.Response.class);
        assertThat(response.getResult().getSpecies(), allOf(
                hasProperty("id", not(emptyOrNullString())),
                hasProperty("gardenerId", not(emptyOrNullString()))));
        assertThat(response.getResult().getPlants(), containsInAnyOrder(
                allOf(
                        hasProperty("id", not(emptyOrNullString())),
                        hasProperty("gardenerId", not(emptyOrNullString())),
                        hasProperty("speciesId", not(emptyOrNullString())),
                        hasProperty("garden", equalTo("Garden 1"))),
                allOf(
                        hasProperty("id", not(emptyOrNullString())),
                        hasProperty("gardenerId", not(emptyOrNullString())),
                        hasProperty("speciesId", not(emptyOrNullString())),
                        hasProperty("garden", equalTo("Garden 2")))));
    }

    // TODO test create with existing gardener
    // TODO test save with delete
    // TODO test empty request?

    @Test
    void noAuthentication() {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody("{}");
        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(403));
    }
}
