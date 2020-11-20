package gardenmanager.webapp.plant;

import java.util.List;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.*;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.species.DependencyFactory;
import gardenmanager.webapp.util.DataFactory;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.MockCognito;
import gardenmanager.webapp.util.MockContext;
import org.hamcrest.Matcher;
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
        var gardenerIdIs = not(emptyOrNullString());
        createPlants("foo@example.com", gardenerIdIs);
    }

    @Test
    void createPlantsForExistingGardener() throws Exception {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);
        createPlants(email, is(gardener.getId()));
    }

    private void createPlants(final String email, final Matcher<String> gardenerMatcher) throws Exception {
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
        Species species = response.getResult().getSpecies();
        assertThat(species, allOf(
                hasProperty("id", not(emptyOrNullString())),
                hasProperty("gardenerId", gardenerMatcher)));
        assertThat(response.getResult().getPlants(), containsInAnyOrder(
                allOf(
                        hasProperty("id", not(emptyOrNullString())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden 1"))),
                allOf(
                        hasProperty("id", not(emptyOrNullString())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden 2")))));
    }

    @Test
    void updateSpeciesAndCreatePlants() throws Exception {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);
        Species species = DataFactory.species(gardener.getId());
        deps.speciesComp().save(species);

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        final EditPlantsLambda.Request request = new EditPlantsLambda.Request();
        request.setSpecies(species);
        request.setPlants(List.of(
                DataFactory.plant(null, null, "Garden 1"),
                DataFactory.plant(null, null, "Garden 2")));
        input.setBody(JsonUtils.toJson(request));

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(201));

        EditPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), EditPlantsLambda.Response.class);
        assertThat(response.getResult().getSpecies(), allOf(
                hasProperty("id", is(species.getId())),
                hasProperty("gardenerId", is(gardener.getId()))));
        assertThat(response.getResult().getPlants(), containsInAnyOrder(
                allOf(
                        hasProperty("id", not(emptyOrNullString())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden 1"))),
                allOf(
                        hasProperty("id", not(emptyOrNullString())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden 2")))));
    }

    @Test
    void updateSpeciesAndUpdatePlants() throws Exception {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);
        Species species = DataFactory.species(gardener.getId());
        deps.speciesComp().save(species);

        List<Plant> plants = List.of(
                DataFactory.plant(gardener.getId(), species.getId(), "Garden 1"),
                DataFactory.plant(gardener.getId(), species.getId(), "Garden 2"));
        plants.forEach(deps.plantComp()::save);

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        species.setName("Species Uno");
        species.setAlternateName("Alt species uno");
        plants.get(0).setGarden("Garden Uno");
        plants.get(1).setGarden("Garden Dos");

        final EditPlantsLambda.Request request = new EditPlantsLambda.Request();
        request.setSpecies(species);
        request.setPlants(plants);
        input.setBody(JsonUtils.toJson(request));

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(201));

        SpeciesWithPlants readPlants = deps.plantComp().findPlantsBySpeciesId(species.getId()).get();
        assertThat(readPlants.getSpecies(), allOf(
                hasProperty("id", is(species.getId())),
                hasProperty("gardenerId", is(gardener.getId())),
                hasProperty("name", is("Species Uno")),
                hasProperty("alternateName", is("Alt species uno")),
                hasProperty("moisture", is(MoisturePreference.MEDIUM)),
                hasProperty("light", is(LightPreference.FULL))));
        assertThat(readPlants.getPlants(), containsInAnyOrder(
                allOf(
                        hasProperty("id", is(plants.get(0).getId())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden Uno"))),
                allOf(
                        hasProperty("id", is(plants.get(1).getId())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden Dos")))));
    }

    @Test
    void deletePlants() throws Exception {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);
        Species species = DataFactory.species(gardener.getId());
        deps.speciesComp().save(species);

        List<Plant> plants = List.of(
                DataFactory.plant(gardener.getId(), species.getId(), "Garden 1"),
                DataFactory.plant(gardener.getId(), species.getId(), "Garden 2"),
                DataFactory.plant(gardener.getId(), species.getId(), "Garden 3"));
        plants.forEach(deps.plantComp()::save);

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        final EditPlantsLambda.Request request = new EditPlantsLambda.Request();
        request.setSpecies(species);
        request.setPlants(List.of(plants.get(1)));
        request.setPlantsToDelete(List.of(plants.get(0), plants.get(2)));
        input.setBody(JsonUtils.toJson(request));

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(201));

        SpeciesWithPlants readPlants = deps.plantComp().findPlantsBySpeciesId(species.getId()).get();
        assertThat(readPlants.getSpecies(), allOf(
                hasProperty("id", is(species.getId())),
                hasProperty("gardenerId", is(gardener.getId()))));
        assertThat(readPlants.getPlants(), containsInAnyOrder(
                allOf(
                        hasProperty("id", is(plants.get(1).getId())),
                        hasProperty("gardenerId", is(species.getGardenerId())),
                        hasProperty("speciesId", is(species.getId())),
                        hasProperty("garden", equalTo("Garden 2")))));
    }

    // TODO test save with delete

    @Test
    void minimalRequest() throws Exception {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, "foo@example.com");

        input.setBody("{\"species\":{\"name\":\"\"}}");

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(201));

        EditPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), EditPlantsLambda.Response.class);

        assertThat(response.getResult().getSpecies(), allOf(
                hasProperty("id", not(emptyOrNullString())),
                hasProperty("gardenerId", not(emptyOrNullString()))));
        assertThat(response.getResult().getPlants(), hasSize(0));
    }

    @Test
    void emptyRequest() throws Exception {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, "foo@example.com");

        input.setBody("{}");

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(400));
    }

    @Test
    void noAuthentication() {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody("{\"species\":{\"name\":\"\"}}");
        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(input, new MockContext());
        assertThat(responseEvent.getStatusCode(), is(403));
    }
}
