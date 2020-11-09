package com.ohboywerecamping.webapp.gardener;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.ohboywerecamping.domain.Gardener;
import com.ohboywerecamping.gardener.GardenerComponent;
import com.ohboywerecamping.webapp.util.Cognito;
import com.ohboywerecamping.webapp.util.JsonUtils;
import com.ohboywerecamping.webapp.util.Ok;

import static com.ohboywerecamping.webapp.util.Responses.created;

public class CreateGardenerLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final GardenerComponent gardeners;

    public CreateGardenerLambda(final GardenerComponent gardeners) {
        this.gardeners = gardeners;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        context.getLogger().log("Received event in " + getClass().getSimpleName());

        context.getLogger().log("Authenticated username is  " + Cognito.username(input).orElse(null));

        final Optional<String> email = Cognito.username(input);

        final Optional<Gardener> gardener = email.flatMap(gardeners::findGardenerByEmail);

        if (email.isPresent() && gardener.isEmpty()) {
            gardeners.findOrCreateGardener(email.get());
        }

        return created(JsonUtils.toJson(new Ok()));
    }
}
