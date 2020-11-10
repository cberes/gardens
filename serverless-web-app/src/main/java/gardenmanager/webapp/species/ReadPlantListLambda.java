package gardenmanager.webapp.species;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;

import static gardenmanager.webapp.util.Responses.ok;

public class ReadPlantListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Response {
        private final List<Species> species;

        public Response(final List<Species> species) {
            this.species = species;
        }

        public List<Species> getPlants() {
            return species;
        }
    }

    private final SpeciesComponent plants;
    private final GardenerComponent gardeners;

    public ReadPlantListLambda(final SpeciesComponent plants, final GardenerComponent gardeners) {
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

        final List<Species> found = plants.findPlantsByGardenerId(gardenerId);
        return ok(JsonUtils.toJson(new Response(found)));
    }
}
