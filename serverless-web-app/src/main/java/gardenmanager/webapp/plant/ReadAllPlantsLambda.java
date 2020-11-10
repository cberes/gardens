package gardenmanager.webapp.plant;

import java.util.List;
import java.util.Map;

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
import gardenmanager.webapp.util.JsonUtils;

import static gardenmanager.webapp.util.Responses.ok;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class ReadAllPlantsLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class SpeciesPlants {
        private final Species species;
        private final List<Plant> plants;

        public SpeciesPlants(final Species species, final List<Plant> plants) {
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

    public static class Response {
        private final List<SpeciesPlants> plants;

        public Response(final List<SpeciesPlants> plants) {
            this.plants = plants;
        }

        public List<SpeciesPlants> getPlants() {
            return plants;
        }
    }

    private final SpeciesComponent species;
    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public ReadAllPlantsLambda(final SpeciesComponent species,
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

        final String gardenerId = Cognito.username(input)
                .flatMap(gardeners::findGardenerByEmail)
                .map(Gardener::getId)
                .orElse("example");

        final List<Species> found = species.findSpeciesByGardenerId(gardenerId);
        final Map<String, List<Plant>> plantsBySpeciesId = plants.findPlantsBySpeciesId(gardenerId)
                .stream().collect(groupingBy(Plant::getSpeciesId));

        final List<SpeciesPlants> allPlants = found.stream()
                .map(spec -> new SpeciesPlants(spec, plantsBySpeciesId.getOrDefault(spec.getId(), emptyList())))
                .collect(toList());

        return ok(JsonUtils.toJson(new Response(allPlants)));
    }
}
