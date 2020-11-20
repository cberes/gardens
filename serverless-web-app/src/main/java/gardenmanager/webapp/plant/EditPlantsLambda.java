package gardenmanager.webapp.plant;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;
import gardenmanager.domain.SpeciesWithPlants;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.plant.PlantComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.*;

import static java.util.Collections.emptyList;

public class EditPlantsLambda implements ApiRequestHandler {
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

        @JsonCreator
        public Response(@JsonProperty("result") final SpeciesWithPlants result) {
            this.result = result;
        }

        public SpeciesWithPlants getResult() {
            return result;
        }
    }

    private static class GardenerNotFoundException extends Exception {}

    private static class MissingFieldException extends Exception {
        public MissingFieldException(final String message) {
            super(message);
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

        try {
            final Request request = jackson.readValue(input.getBody(), Request.class);
            requireSpecies(request);
            final String username = Cognito.username(input).orElseThrow(GardenerNotFoundException::new);
            return handleRequestAuthenticated(request, username);
        } catch (IOException e) {
            return Responses.badRequest(e.getMessage());
        } catch (MissingFieldException e) {
            return Responses.badRequest(e.getMessage());
        } catch (GardenerNotFoundException e) {
            return Responses.forbidden(JsonUtils.toJson(new ErrorMessage("Forbidden")));
        }
    }

    private static void requireSpecies(final Request request) throws MissingFieldException {
        if (request.getSpecies() == null) {
            throw new MissingFieldException("Species is required");
        }

        if (request.getSpecies().getName() == null) {
            throw new MissingFieldException("Species name is required");
        }
    }

    private APIGatewayProxyResponseEvent handleRequestAuthenticated(final Request request, final String username) {
        final Gardener gardener = gardeners.findOrCreateGardener(username);

        saveSpecies(request, gardener);

        savePlants(request, gardener);

        return Responses.created(JsonUtils.toJson(new Response(
                new SpeciesWithPlants(request.getSpecies(), request.getPlants()))));
    }

    private void saveSpecies(final Request request, final Gardener gardener) {
        request.getSpecies().setGardenerId(gardener.getId());
        species.save(request.getSpecies());
    }

    private void savePlants(final Request request, final Gardener gardener) {
        setIds(request, gardener);
        request.getPlants().forEach(plants::save);
        request.getPlantsToDelete().forEach(plants::delete);
    }

    private void setIds(final Request request, final Gardener gardener) {
        request.getPlants().forEach(plant -> plant.setGardenerId(gardener.getId()));
        request.getPlants().forEach(plant -> plant.setSpeciesId(request.getSpecies().getId()));
        request.getPlantsToDelete().forEach(plant -> plant.setGardenerId(gardener.getId()));
    }
}
