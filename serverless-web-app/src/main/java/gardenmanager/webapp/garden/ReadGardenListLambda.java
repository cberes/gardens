package gardenmanager.webapp.garden;

import java.util.List;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Garden;
import gardenmanager.domain.Gardener;
import gardenmanager.garden.GardenComponent;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

import static gardenmanager.webapp.util.Responses.ok;
import static java.util.Collections.emptyList;

public class ReadGardenListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Response {
        private final List<Garden> gardens;

        public Response(final List<Garden> gardens) {
            this.gardens = gardens;
        }

        public List<Garden> getGardens() {
            return gardens;
        }
    }

    private final GardenComponent gardens;
    private final GardenerComponent gardeners;

    public ReadGardenListLambda(final GardenComponent gardens, final GardenerComponent gardeners) {
        this.gardens = gardens;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final Optional<Gardener> gardener = Cognito.username(input).flatMap(gardeners::findGardenerByEmail);
        final List<Garden> found = gardener.map(Gardener::getId).map(gardens::findGardensByGardenerId).orElse(emptyList());
        return Responses.ok(JsonUtils.toJson(new Response(found)));
    }
}
