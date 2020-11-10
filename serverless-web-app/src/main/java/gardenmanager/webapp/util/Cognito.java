package gardenmanager.webapp.util;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class Cognito {
    private Cognito() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    @SuppressWarnings("unchecked")
    public static Optional<String> username(final APIGatewayProxyRequestEvent input) {
        final var authorizer = input.getRequestContext().getAuthorizer();
        if (authorizer == null) {
            return Optional.empty();
        }

        final var claims = (Map<String, ?>) authorizer.get("claims");
        return Optional.ofNullable(claims)
                .map(it -> it.get("cognito:username"))
                .map(Object::toString);
    }
}
