package gardenmanager.webapp.plant;

import java.util.List;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gardenmanager.domain.Garden;
import gardenmanager.domain.Gardener;
import gardenmanager.plant.PlantComponent;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.webapp.util.ApiRequestHandler;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

import static java.util.Collections.emptyList;

public class ReadGardenListLambda implements ApiRequestHandler {
    public static class Response {
        private final List<Garden> gardens;

        @JsonCreator
        public Response(@JsonProperty("gardens") final List<Garden> gardens) {
            this.gardens = gardens;
        }

        public List<Garden> getGardens() {
            return gardens;
        }
    }

    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadGardenListLambda(final PlantComponent plants, final GardenerComponent gardeners) {
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final Optional<Gardener> gardener = Cognito.username(input)
                .flatMap(gardeners::findGardenerByEmail);

        final List<Garden> found = gardener
                .map(Gardener::getId)
                .map(plants::findGardensByGardenerId)
                .orElse(emptyList());

        return Responses.ok(JsonUtils.toJson(new Response(found)));
    }
}
