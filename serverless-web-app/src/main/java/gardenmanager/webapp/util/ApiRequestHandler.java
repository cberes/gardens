package gardenmanager.webapp.util;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface ApiRequestHandler
        extends RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
}
