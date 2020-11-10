package gardenmanager.webapp.species;

import java.io.IOException;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import gardenmanager.domain.Gardener;
import gardenmanager.domain.Species;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.webapp.util.Cognito;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;

public class EditPlantLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static class Request {
        private Species species;

        public Species getPlant() {
            return species;
        }

        public void setPlant(final Species species) {
            this.species = species;
        }
    }

    public static class Response {
        private final Species species;

        public Response(final Species species) {
            this.species = species;
        }

        public Species getPlant() {
            return species;
        }
    }

    private final ObjectMapper jackson;
    private final SpeciesComponent plants;
    private final GardenerComponent gardeners;

    public EditPlantLambda(final ObjectMapper jackson, final SpeciesComponent plants, final GardenerComponent gardeners) {
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
            return Responses.badRequest(e.getMessage());
        }

        final Optional<Gardener> gardener = Cognito.username(input).flatMap(gardeners::findGardenerByEmail);

        final Species species = request.getPlant();
        gardener.map(Gardener::getId).ifPresent(species::setGardenerId);
        plants.save(species);
        return Responses.created(JsonUtils.toJson(new Response(species)));
    }
}
