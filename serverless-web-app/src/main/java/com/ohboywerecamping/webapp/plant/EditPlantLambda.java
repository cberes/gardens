package com.ohboywerecamping.webapp.plant;

import java.io.IOException;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohboywerecamping.domain.Gardener;
import com.ohboywerecamping.domain.Plant;
import com.ohboywerecamping.gardener.GardenerComponent;
import com.ohboywerecamping.plant.PlantComponent;
import com.ohboywerecamping.webapp.util.Cognito;
import com.ohboywerecamping.webapp.util.JsonUtils;

import static com.ohboywerecamping.webapp.util.Responses.badRequest;
import static com.ohboywerecamping.webapp.util.Responses.created;

public class EditPlantLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Request {
        private Plant plant;

        public Plant getPlant() {
            return plant;
        }

        public void setPlant(final Plant plant) {
            this.plant = plant;
        }
    }

    public static class Response {
        private final Plant plant;

        public Response(final Plant plant) {
            this.plant = plant;
        }

        public Plant getPlant() {
            return plant;
        }
    }

    private final ObjectMapper jackson;
    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public EditPlantLambda(final ObjectMapper jackson, final PlantComponent plants, final GardenerComponent gardeners) {
        this.jackson = jackson;
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final Request request;
        try {
            request = jackson.readValue(input.getBody(), Request.class);
        } catch (IOException e) {
            return badRequest(e.getMessage());
        }

        final Optional<Gardener> gardener = Cognito.username(input).flatMap(gardeners::findGardenerByEmail);

        final Plant plant = request.getPlant();
        gardener.map(Gardener::getId).ifPresent(plant::setGardenerId);
        plants.save(plant);
        return created(JsonUtils.toJson(new Response(plant)));
    }
}
