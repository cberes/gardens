package gardenmanager.webapp.plant;

import java.util.List;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.ErrorMessage;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

public class ReadPlantsLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Response {
        private final Species species;
        private List<Plant> plants;

        public Response(final Species species, final List<Plant> plants) {
            this.species = species;
            this.plants = plants;
        }

        public Species getSpecies() {
            return species;
        }

        public List<Plant> getPlants() {
            return plants;
        }
    }

    private final SpeciesComponent species;
    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadPlantsLambda(final SpeciesComponent species,
                            final PlantComponent plants,
                            final GardenerComponent gardeners) {
        this.species = species;
        this.plants = plants;
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final String speciesId = input.getPathParameters().get("speciesId");

        final String gardenerId = Cognito.username(input)
                .flatMap(gardeners::findGardenerByEmail)
                .map(Gardener::getId)
                .orElse("example");

        final Optional<Species> found = species.findSpeciesById(speciesId).filter(plant -> plant.getGardenerId().equals(gardenerId));

        if (found.isEmpty()) {
            return Responses.notFound(JsonUtils.toJson(new ErrorMessage("Species not found: " + speciesId)));
        }

        return Responses.ok(JsonUtils.toJson(new Response(
                found.get(), plants.findPlantsBySpeciesId(speciesId))));
    }
}
