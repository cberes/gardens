package gardenmanager.webapp.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import static java.util.Collections.singletonMap;

/**
 * Common HTTP responses.
 */
public final class Responses {
    private Responses() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static APIGatewayProxyResponseEvent ok(final String body) {
        return response(200, body);
    }

    public static APIGatewayProxyResponseEvent ok() {
        return response(204, null);
    }

    public static APIGatewayProxyResponseEvent created(final String body) {
        return response(201, body);
    }

    public static APIGatewayProxyResponseEvent badRequest(final String body) {
        return response(400, body);
    }

    public static APIGatewayProxyResponseEvent forbidden(final String body) {
        return response(403, body);
    }

    public static APIGatewayProxyResponseEvent notFound(final String body) {
        return response(404, body);
    }

    public static APIGatewayProxyResponseEvent serverError(final String body) {
        return response(500, body);
    }

    private static APIGatewayProxyResponseEvent response(final int status, final String body) {
        final APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setBody(body);
        response.setHeaders(singletonMap("Access-Control-Allow-Origin", "*"));
        response.setStatusCode(status);
        return response;
    }
}
