package gardenmanager.webapp.plant;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.webapp.util.DataFactory;
import gardenmanager.webapp.util.MockCognito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gardenmanager.webapp.plant.GardenerLookup.gardenerId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class GardenerLookupTest {
    private GardenerComponent mockGardeners;

    @BeforeEach
    void setup() {
        mockGardeners = mock(GardenerComponent.class);
    }

    @Test
    void usesDefaultIdWhenNotAuthenticated() {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        assertThat(gardenerId(input, mockGardeners).get(), is("public"));
    }

    @Test
    void emptyWhenGardenerNotFound() {
        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, "foo@example.com");
        when(mockGardeners.findGardenerByEmail(anyString())).thenReturn(Optional.empty());

        assertThat(gardenerId(input, mockGardeners),
                hasProperty("present", equalTo(false)));
    }

    @Test
    void gardenerWhenGardenerFound() {
        final String gardenerId = "0b0fb05a-bd6a-44af-b77a-d6e822967fa5";
        final String email = "foo@example.com";

        final APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        MockCognito.mockUsername(input, email);
        when(mockGardeners.findGardenerByEmail(email))
                .thenReturn(Optional.of(DataFactory.gardener(gardenerId, email)));

        assertThat(gardenerId(input, mockGardeners).get(), is(gardenerId));
    }
}
