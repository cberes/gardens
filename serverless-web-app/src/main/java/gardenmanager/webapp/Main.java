package gardenmanager.webapp;

import java.util.Map;
import java.util.function.Supplier;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import gardenmanager.webapp.Lambdas.*;
import gardenmanager.webapp.util.ApiRequestHandler;
import gardenmanager.webapp.util.AwsUtils;

/**
 * The main entry point for Lambda functions compiled using Graal.
 * This class uses the name of the handler from the {@code _HANDLER} environment variable
 * to pick the Lambda function to run.
 */
public final class Main {
    public static void main(String[] args) throws Exception {
        run(AwsUtils.handler());
    }

    static void run(final String handlerName) throws Exception {
        final var handler = handlers().get(handlerName);
        if (handler == null) {
            System.err.println("Unknown handler: " + handlerName);
        } else {
            Lambda.handleEvents(handler.get()::handleRequest, APIGatewayProxyRequestEvent.class);
        }
    }

    private static Map<String, Supplier<ApiRequestHandler>> handlers() {
        return Map.of(
                "read_garden_list", LiveReadGardenListLambda::new,
                "create_gardener", LiveCreateGardenerLambda::new,
                "delete_species", LiveDeleteSpeciesLambda::new,
                "edit_plants", LiveEditPlantsLambda::new,
                "read_plants", LiveReadPlantsLambda::new,
                "read_all_plants", LiveReadAllPlantsLambda::new);
    }
}
