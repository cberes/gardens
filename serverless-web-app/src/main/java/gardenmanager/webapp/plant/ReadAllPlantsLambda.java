package gardenmanager.webapp.plant;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.webapp.util.ApiRequestHandler;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

import static java.util.Collections.emptyList;

public class ReadAllPlantsLambda implements ApiRequestHandler {
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

        var results = GardenerLookup.gardenerId(input, gardeners)
                .map(plants::findPlantsByGardenerId)
                .orElse(emptyList());

        return Responses.ok(JsonUtils.toJson(new Response(results)));
    }
}
