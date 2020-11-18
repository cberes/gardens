package gardenmanager.webapp.species;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.util.DataFactory;
import gardenmanager.webapp.util.MockCognito;
import gardenmanager.webapp.util.MockContext;
import gardenmanager.webapp.util.MockParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

@UseTables({"GARDENER_local", "SPECIES_local", "PLANT_local"})
public class DeleteSpeciesLambdaTest {
    @InjectDynamo
    private DynamoDbClient dynamo;
    private DependencyFactory deps;
    private DeleteSpeciesLambda lambda;

    @BeforeEach
    void setup() {
        deps = new DependencyFactory(dynamo);
        lambda = new DeleteSpeciesLambda(deps.speciesComp(), deps.gardenerComp());
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
    void deletesOnlyGivenSpeciesAndItsPlants() {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        Species species = createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        Species otherSpecies = createSpecies(gardener.getId(), "Garden 3");

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);
        MockParameters.mockPathParam(input, "plantId", species.getId());

        APIGatewayProxyResponseEvent response = lambda.handleRequest(input, new MockContext());
        assertThat(response.getStatusCode(), is(200));

        assertThat(deps.speciesRepo().findById(species.getId()), hasProperty("present", equalTo(false)));
        assertThat(deps.plantRepo().findAllBySpeciesId(species.getId()), hasSize(0));

        assertThat(deps.speciesRepo().findById(otherSpecies.getId()), hasProperty("present", equalTo(true)));
        assertThat(deps.plantRepo().findAllBySpeciesId(otherSpecies.getId()), hasSize(1));
    }

    @Test
    void doesNotDeleteSpeciesBelongingToOtherGardener() {
        final String email = "foo@example.com";
        Gardener gardener = deps.gardenerComp().findOrCreateGardener(email);

        Species species = createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);
        MockParameters.mockPathParam(input, "plantId", species.getId());

        APIGatewayProxyResponseEvent response = lambda.handleRequest(input, new MockContext());
        assertThat(response.getStatusCode(), is(404));

        assertThat(deps.speciesRepo().findById(species.getId()), hasProperty("present", equalTo(true)));
        assertThat(deps.plantRepo().findAllBySpeciesId(species.getId()), hasSize(2));
    }

    @Test
    void deleteWhenEmptyDatabase() {
        final String email = "foo@example.com";

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);
        MockParameters.mockPathParam(input, "plantId", "dW5rbm93bg==:unknown");

        APIGatewayProxyResponseEvent response = lambda.handleRequest(input, new MockContext());
        assertThat(response.getStatusCode(), is(404));
    }
}
