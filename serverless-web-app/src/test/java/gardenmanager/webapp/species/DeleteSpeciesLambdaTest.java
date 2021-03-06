package gardenmanager.webapp.species;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
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

    @Test
    void deletesOnlyGivenSpeciesAndItsPlants() {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");

        final Species species = deps.plantFactory().createSpecies(gardener.getId(), "Garden 1", "Garden 2");
        final Species otherSpecies = deps.plantFactory().createSpecies(gardener.getId(), "Garden 3");

        final APIGatewayProxyRequestEvent input = execute(gardener.getEmail(), species.getId());

        final APIGatewayProxyResponseEvent response = lambda.handleRequest(input, new MockContext());
        assertThat(response.getStatusCode(), is(200));

        assertThat(deps.speciesRepo().findById(species.getId()), hasProperty("present", equalTo(false)));
        assertThat(deps.plantRepo().findAllBySpeciesId(species.getId()), hasSize(0));

        assertThat(deps.speciesRepo().findById(otherSpecies.getId()), hasProperty("present", equalTo(true)));
        assertThat(deps.plantRepo().findAllBySpeciesId(otherSpecies.getId()), hasSize(1));
    }

    private APIGatewayProxyRequestEvent execute(final String email, final String speciesId) {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);
        MockParameters.mockPathParam(input, "speciesId", speciesId);
        return input;
    }

    @Test
    void doesNotDeleteSpeciesBelongingToOtherGardener() {
        final Gardener gardener = deps.gardenerComp().findOrCreateGardener("foo@example.com");

        final Species species = deps.plantFactory().createSpecies("other" + gardener.getId(), "Garden 1", "Garden 2");

        final APIGatewayProxyRequestEvent input = execute(gardener.getEmail(), species.getId());

        final APIGatewayProxyResponseEvent response = lambda.handleRequest(input, new MockContext());
        assertThat(response.getStatusCode(), is(404));

        assertThat(deps.speciesRepo().findById(species.getId()), hasProperty("present", equalTo(true)));
        assertThat(deps.plantRepo().findAllBySpeciesId(species.getId()), hasSize(2));
    }

    @Test
    void deleteWhenEmptyDatabase() {
        final APIGatewayProxyRequestEvent input = execute("foo@example.com", "dW5rbm93bg==:unknown");

        final APIGatewayProxyResponseEvent response = lambda.handleRequest(input, new MockContext());
        assertThat(response.getStatusCode(), is(404));
    }
}
