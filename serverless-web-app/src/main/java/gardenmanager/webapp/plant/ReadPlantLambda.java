package gardenmanager.webapp.plant;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Plant;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.ErrorMessage;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

import static gardenmanager.webapp.util.Responses.ok;

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
            return Responses.notFound(JsonUtils.toJson(new ErrorMessage("Plant not found: " + plantId)));
        }

        return Responses.ok(JsonUtils.toJson(new Response(found.get())));
    }
}
