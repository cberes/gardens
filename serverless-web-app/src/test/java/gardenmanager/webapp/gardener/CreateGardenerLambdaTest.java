package gardenmanager.webapp.gardener;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.gardener.GardenerComponentImpl;
import gardenmanager.gardener.GardenerRepository;
import gardenmanager.webapp.dynamo.InjectDynamo;
import gardenmanager.webapp.dynamo.UseTables;
import gardenmanager.webapp.util.MockCognito;
import gardenmanager.webapp.util.MockContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;

@UseTables("GARDENER_local")
public class CreateGardenerLambdaTest {
    @InjectDynamo
    private DynamoDbClient dynamo;
    private GardenerRepository repo;
    private GardenerComponent comp;
    private CreateGardenerLambda lambda;

    @BeforeEach
    void setup() {
        repo = new DynamoGardenerRepository(dynamo);
        comp = new GardenerComponentImpl(repo);
        lambda = new CreateGardenerLambda(comp);
    }

    @Test
    void testFindOrCreateCreatesGardener() {
        final String email = "foo@example.com";
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);

        assertThat(comp.findGardenerByEmail(email), hasProperty("present", equalTo(false)));

        lambda.handleRequest(input, new MockContext());

        assertThat(comp.findGardenerByEmail(email), hasProperty("present", equalTo(true)));
    }
}
