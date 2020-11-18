package gardenmanager.webapp.plant;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

import static java.util.Collections.emptyList;

public class EditPlantsLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Request {
        private Species species;
        private List<Plant> plants = emptyList();
        private List<Plant> plantsToDelete = emptyList();

        public Species getSpecies() {
            return species;
        }

        public void setSpecies(final Species species) {
            this.species = species;
        }

        public List<Plant> getPlants() {
            return plants;
        }

        public void setPlants(final List<Plant> plants) {
            this.plants = plants;
        }

        public List<Plant> getPlantsToDelete() {
            return plantsToDelete;
        }

        public void setPlantsToDelete(final List<Plant> plantsToDelete) {
            this.plantsToDelete = plantsToDelete;
        }
    }

    public static class Response {
        private final SpeciesWithPlants result;

        public Response(final SpeciesWithPlants result) {
            this.result = result;
        }

        public SpeciesWithPlants getResult() {
            return result;
        }
    }

    private final ObjectMapper jackson;
    private final SpeciesComponent species;
    private final PlantComponent plants;
    private final GardenerComponent gardeners;

    public EditPlantsLambda(final ObjectMapper jackson,
                            final SpeciesComponent species,
                            final PlantComponent plants,
                            final GardenerComponent gardeners) {
        this.jackson = jackson;
        this.species = species;
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
            return Responses.badRequest(e.getMessage());
        }

        final Gardener gardener = gardeners.findOrCreateGardener(Cognito.username(input).get());

        setGardenerIds(gardener, request);

        species.save(request.getSpecies());
        request.getPlants().forEach(plants::save);
        request.getPlantsToDelete().forEach(plants::delete);

        return Responses.created(JsonUtils.toJson(new Response(
                new SpeciesWithPlants(request.getSpecies(), request.getPlants()))));
    }

    private void setGardenerIds(final Gardener gardener, final Request request) {
        request.getSpecies().setGardenerId(gardener.getId());
        request.getPlants().forEach(plant -> plant.setGardenerId(gardener.getId()));
        request.getPlantsToDelete().forEach(plant -> plant.setGardenerId(gardener.getId()));
    }
}
