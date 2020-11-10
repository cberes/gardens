package gardenmanager.webapp.plant;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Plant;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;

import static gardenmanager.webapp.util.Responses.ok;

public class ReadPlantListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Response {
        private final List<Plant> plants;

        public Response(final List<Plant> plants) {
            this.plants = plants;
        }

        public List<Plant> getPlants() {
            return plants;
        }
    }

    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadPlantListLambda(final PlantComponent plants, final GardenerComponent gardeners) {
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
                .orElse("example");

        final List<Plant> found = plants.findPlantsByGardenerId(gardenerId);
        return ok(JsonUtils.toJson(new Response(found)));
    }
}
