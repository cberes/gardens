package com.ohboywerecamping.webapp.plant;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ohboywerecamping.domain.Gardener;
import com.ohboywerecamping.domain.Plant;
import com.ohboywerecamping.gardener.GardenerComponent;
import com.ohboywerecamping.plant.PlantComponent;
import com.ohboywerecamping.webapp.util.Cognito;
import com.ohboywerecamping.webapp.util.ErrorMessage;
import com.ohboywerecamping.webapp.util.JsonUtils;

import static com.ohboywerecamping.webapp.util.Responses.notFound;
import static com.ohboywerecamping.webapp.util.Responses.ok;

public class ReadPlantLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Response {
        private final Plant plant;

        public Response(final Plant plant) {
            this.plant = plant;
        }

        public Plant getPlant() {
            return plant;
        }
    }

    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadPlantLambda(final PlantComponent plants, final GardenerComponent gardeners) {
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final String plantId = input.getPathParameters().get("plantId");

        final String gardenerId = Cognito.username(input)
                .flatMap(gardeners::findGardenerByEmail)
                .map(Gardener::getId)
                .orElse("example");

        final Optional<Plant> found = plants.findPlantById(plantId).filter(plant -> plant.getGardenerId().equals(gardenerId));

        if (found.isEmpty()) {
            return notFound(JsonUtils.toJson(new ErrorMessage("Plant not found: " + plantId)));
        }

        return ok(JsonUtils.toJson(new Response(found.get())));
    }
}
