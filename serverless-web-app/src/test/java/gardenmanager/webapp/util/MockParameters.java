package gardenmanager.webapp.util;

import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public final class MockParameters {
    private MockParameters() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static void mockPathParam(final APIGatewayProxyRequestEvent input, final String key, final Object value) {
        initPathParameters(input);
        input.getPathParameters().put(key, value.toString());
    }

    private static void initPathParameters(final APIGatewayProxyRequestEvent input) {
        if (input.getPathParameters() == null) {
            input.setPathParameters(new HashMap<>());
        }
    }
}
