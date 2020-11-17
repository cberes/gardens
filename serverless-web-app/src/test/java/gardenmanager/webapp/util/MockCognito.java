package gardenmanager.webapp.util;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public final class MockCognito {
    private MockCognito() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static void mockUsername(final APIGatewayProxyRequestEvent input, final String username) {
        initContext(input);
        initAuthorizer(input);
        Map<String, String> claims = initClaims(input);
        claims.put("cognito:username", username);
    }

    private static void initContext(final APIGatewayProxyRequestEvent input) {
        if (input.getRequestContext() == null) {
            input.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        }
    }

    private static void initAuthorizer(final APIGatewayProxyRequestEvent input) {
        if (input.getRequestContext().getAuthorizer() == null) {
            input.getRequestContext().setAuthorizer(new HashMap<>());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> initClaims(final APIGatewayProxyRequestEvent input) {
        return (Map<String, String>) input.getRequestContext().getAuthorizer()
                .computeIfAbsent("claims", key -> new HashMap<String, String>());
    }
}
