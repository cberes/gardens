package gardenmanager.webapp.species;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.ErrorMessage;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Ok;
import gardenmanager.webapp.util.*;

import static gardenmanager.webapp.util.Responses.ok;

public class DeletePlantLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final SpeciesComponent plants;
    private final GardenerComponent gardeners;

    public DeletePlantLambda(final SpeciesComponent plants, final GardenerComponent gardeners) {
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final String plantId = input.getPathParameters().get("plantId");

        final Optional<Gardener> gardener = Cognito.username(input).flatMap(gardeners::findGardenerByEmail);
        final Optional<Species> found = gardener.map(Gardener::getId).flatMap(gardenerId ->
                plants.findPlantById(plantId).filter(plant -> plant.getGardenerId().equals(gardenerId)));

        if (found.isEmpty()) {
            return Responses.notFound(JsonUtils.toJson(new ErrorMessage("Plant not found: " + plantId)));
        }

        plants.delete(found.get());
        return Responses.ok(JsonUtils.toJson(new Ok()));
    }
}
