package gardenmanager.webapp.plant;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.webapp.util.*;

public class ReadPlantsLambda implements ApiRequestHandler {
    public static class Response {
        private final SpeciesWithPlants result;

        @JsonCreator
        public Response(@JsonProperty("result") final SpeciesWithPlants result) {
            this.result = result;
        }

        public SpeciesWithPlants getResult() {
            return result;
        }
    }

    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadPlantsLambda(final PlantComponent plants,
                            final GardenerComponent gardeners) {
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final String speciesId = input.getPathParameters().get("speciesId");

        final Optional<SpeciesWithPlants> found = GardenerLookup.gardenerId(input, gardeners)
                .flatMap(gardenerId -> plants.findPlantsBySpeciesId(speciesId)
                        .filter(it -> it.getSpecies().getGardenerId().equals(gardenerId)));

        if (found.isEmpty()) {
            return Responses.notFound(JsonUtils.toJson(new ErrorMessage("Species not found: " + speciesId)));
        }

        return Responses.ok(JsonUtils.toJson(new Response(found.get())));
    }
}
