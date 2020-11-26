package gardenmanager.webapp.species;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
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

public class DeleteSpeciesLambda implements ApiRequestHandler {
    private final SpeciesComponent species;
    private final GardenerComponent gardeners;

    public DeleteSpeciesLambda(final SpeciesComponent species, final GardenerComponent gardeners) {
        this.species = species;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final String speciesId = input.getPathParameters().get("speciesId");

        final Optional<Gardener> gardener = Cognito.username(input).flatMap(gardeners::findGardenerByEmail);
        final Optional<Species> found = gardener.map(Gardener::getId).flatMap(gardenerId ->
                species.findSpeciesById(speciesId).filter(plant -> plant.getGardenerId().equals(gardenerId)));

        if (found.isEmpty()) {
            return Responses.notFound(JsonUtils.toJson(new ErrorMessage("Plant not found: " + speciesId)));
        }

        species.delete(found.get());
        return Responses.ok(JsonUtils.toJson(new Ok()));
    }
}
