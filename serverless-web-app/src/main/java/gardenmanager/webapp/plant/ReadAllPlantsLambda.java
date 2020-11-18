package gardenmanager.webapp.plant;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;

import static gardenmanager.webapp.util.Responses.ok;

public class ReadAllPlantsLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Response {
        private final List<SpeciesWithPlants> results;

        @JsonCreator
        public Response(@JsonProperty("results") final List<SpeciesWithPlants> results) {
            this.results = results;
        }

        public List<SpeciesWithPlants> getResults() {
            return results;
        }
    }

    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadAllPlantsLambda(final PlantComponent plants,
                               final GardenerComponent gardeners) {
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final String gardenerId = Cognito.username(input)
                .flatMap(gardeners::findGardenerByEmail)
                .map(Gardener::getId)
                .orElse("public");

        return ok(JsonUtils.toJson(new Response(plants.findPlantsByGardenerId(gardenerId))));
    }
}
