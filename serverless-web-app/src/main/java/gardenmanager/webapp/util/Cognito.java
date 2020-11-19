package gardenmanager.webapp.util;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

/**
 * Amazon Cognito utility class.
 */
public class Cognito {
    private Cognito() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    @SuppressWarnings("unchecked")
    public static Optional<String> username(final APIGatewayProxyRequestEvent input) {
        return Optional.ofNullable(input.getRequestContext())
                .map(context -> context.getAuthorizer())
                .map(authorizer -> (Map<String, ?>) authorizer.get("claims"))
                .map(claims -> claims.get("cognito:username"))
                .map(Object::toString);
    }
}
