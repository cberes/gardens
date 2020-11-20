package gardenmanager.webapp.gardener;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.webapp.util.*;

public class CreateGardenerLambda implements ApiRequestHandler {
    private final GardenerComponent gardeners;

    public CreateGardenerLambda(final GardenerComponent gardeners) {
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final Optional<String> email = Cognito.username(input);

        final Optional<Gardener> gardener = email.flatMap(gardeners::findGardenerByEmail);

        if (email.isPresent() && gardener.isEmpty()) {
            gardeners.findOrCreateGardener(email.get());
        }

        return Responses.created(JsonUtils.toJson(new Ok()));
    }
}
