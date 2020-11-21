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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
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
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");
        createPlants(gardener.getEmail(), is(gardener.getId()));
    }

    private void createPlants(final String email, final Matcher<String> gardenerMatcher) throws Exception {
        final Species inputSpecies = DataFactory.species(null);
        final List<Plant> plants = DataFactory.plants(null, null, "Garden 1", "Garden 2");
        final EditPlantsLambda.Request request = makeRequest(inputSpecies, plants);

        final APIGatewayProxyResponseEvent responseEvent = execute(email, JsonUtils.toJson(request));
        assertThat(responseEvent.getStatusCode(), is(201));

        final EditPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), EditPlantsLambda.Response.class);
        final Species species = response.getResult().getSpecies();
        assertThat(species, allOf(
                hasProperty("id", not(emptyOrNullString())),
                hasProperty("gardenerId", gardenerMatcher)));
        assertThat(response.getResult().getPlants(), matches(species, plants, false));
    }

    private APIGatewayProxyResponseEvent execute(final String email, final String body) {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody(body);

        if (email != null) {
            MockCognito.mockUsername(input, email);
        }

        return lambda.handleRequest(input, new MockContext());
    }

    @Test
    void updateSpeciesAndCreatePlants() throws Exception {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");
        final Species species = DataFactory.species(gardener.getId());
        deps.speciesComp().save(species);

        final List<Plant> plants = DataFactory.plants(null, null, "Garden 1", "Garden 2");
        final EditPlantsLambda.Request request = makeRequest(species, plants);

        final APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail(), JsonUtils.toJson(request));
        assertThat(responseEvent.getStatusCode(), is(201));

        final EditPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), EditPlantsLambda.Response.class);
        assertThat(response.getResult().getSpecies(), matches(species, gardener));
        assertThat(response.getResult().getPlants(), matches(species, plants, false));
    }

    private static Matcher<Species> matches(final Species species, final Gardener gardener) {
        return allOf(
                hasProperty("id", is(species.getId())),
                hasProperty("gardenerId", is(gardener.getId())),
                hasProperty("name", is(species.getName())),
                hasProperty("alternateName", is(species.getAlternateName())),
                hasProperty("moisture", is(species.getMoisture())),
                hasProperty("light", is(species.getLight())));
    }

    private static Matcher<Iterable<? extends Plant>> matches(final Species species,
                                                              final List<Plant> plants,
                                                              final boolean matchId) {
        return containsInAnyOrder(plants.stream()
                .map(plant -> hasPlant(species, plant, matchId))
                .collect(toList()));
    }

    private static Matcher<Plant> hasPlant(final Species species, final Plant plant, final boolean matchId) {
        return allOf(
                hasProperty("id", matchId ? is(plant.getId()) : not(emptyOrNullString())),
                hasProperty("gardenerId", is(species.getGardenerId())),
                hasProperty("speciesId", is(species.getId())),
                hasProperty("garden", is(plant.getGarden())));
    }

    @Test
    void updateSpeciesAndUpdatePlants() {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");
        final Species species = DataFactory.species(gardener.getId());
        deps.speciesComp().save(species);

        final List<Plant> plants = DataFactory.plants(gardener.getId(), species.getId(), "Garden 1", "Garden 2");
        plants.forEach(deps.plantComp()::save);

        modifyPlants(species, plants);

        final EditPlantsLambda.Request request = makeRequest(species, plants);

        final APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail(), JsonUtils.toJson(request));
        assertThat(responseEvent.getStatusCode(), is(201));

        final SpeciesWithPlants readPlants = deps.plantComp().findPlantsBySpeciesId(species.getId()).get();
        assertThat(readPlants.getSpecies(), matches(species, gardener));
        assertThat(readPlants.getPlants(), matches(species, plants, true));
    }

    private static void modifyPlants(final Species species, final List<Plant> plants) {
        species.setName("Species Uno");
        species.setAlternateName("Alt species uno");
        plants.get(0).setGarden("Garden Uno");
        plants.get(1).setGarden("Garden Dos");
    }

    @Test
    void deletePlants() {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");
        final Species species = DataFactory.species(gardener.getId());
        deps.speciesComp().save(species);

        List<Plant> plants = DataFactory.plants(gardener.getId(), species.getId(), "Garden 1", "Garden 2", "Garden 3");
        plants.forEach(deps.plantComp()::save);

        final EditPlantsLambda.Request request = makeRequest(species,
                List.of(plants.get(1)), List.of(plants.get(0), plants.get(2)));

        final APIGatewayProxyResponseEvent responseEvent = execute(gardener.getEmail(), JsonUtils.toJson(request));
        assertThat(responseEvent.getStatusCode(), is(201));

        final SpeciesWithPlants readPlants = deps.plantComp().findPlantsBySpeciesId(species.getId()).get();
        assertThat(readPlants.getSpecies(), matches(species, gardener));
        assertThat(readPlants.getPlants(), matches(species, List.of(plants.get(1)), true));
    }

    private static EditPlantsLambda.Request makeRequest(final Species species, final List<Plant> keepPlants) {
        return makeRequest(species, keepPlants, emptyList());
    }

    private static EditPlantsLambda.Request makeRequest(final Species species,
                                                        final List<Plant> keepPlants,
                                                        final List<Plant> deletePlants) {
        final EditPlantsLambda.Request request = new EditPlantsLambda.Request();
        request.setSpecies(species);
        request.setPlants(keepPlants);
        request.setPlantsToDelete(deletePlants);
        return request;
    }

    @Test
    void minimalRequest() throws Exception {
        final String body = "{\"species\":{\"name\":\"\"}}";
        final APIGatewayProxyResponseEvent responseEvent = execute("foo@example.com", body);
        assertThat(responseEvent.getStatusCode(), is(201));

        final EditPlantsLambda.Response response =
                JsonUtils.jackson().readValue(responseEvent.getBody(), EditPlantsLambda.Response.class);

        assertThat(response.getResult().getSpecies(), allOf(
                hasProperty("id", not(emptyOrNullString())),
                hasProperty("gardenerId", not(emptyOrNullString()))));
        assertThat(response.getResult().getPlants(), hasSize(0));
    }

    @Test
    void emptyRequest() {
        final String body = "{}";
        final APIGatewayProxyResponseEvent responseEvent = execute("foo@example.com", body);
        assertThat(responseEvent.getStatusCode(), is(400));
    }

    @Test
    void noAuthentication() {
        final String body = "{\"species\":{\"name\":\"\"}}";
        final APIGatewayProxyResponseEvent responseEvent = execute(null, body);
        assertThat(responseEvent.getStatusCode(), is(403));
    }
}
