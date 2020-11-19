package gardenmanager.webapp.plant;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.webapp.util.Cognito;

final class GardenerLookup {
    private static final String DEFAULT_ID = "public";

    private GardenerLookup() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    static Optional<String> gardenerId(final APIGatewayProxyRequestEvent input, final GardenerComponent gardeners) {
        final Optional<String> username = Cognito.username(input);
        if (username.isEmpty()) {
            return Optional.of(DEFAULT_ID);
        } else {
            return gardeners.findGardenerByEmail(username.get()).map(Gardener::getId);
        }
    }
}
