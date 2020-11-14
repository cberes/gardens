package gardenmanager.webapp;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import gardenmanager.webapp.util.JsonUtils;
import gardenmanager.webapp.util.Responses;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.IoUtils;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * Lambda utility class.
 * Receives Lambda events via HTTP and converts them to event POJOs.
 */
public final class Lambda {
    public static class LambdaError {
        public final String errorMessage;
        public final String errorType;

        public LambdaError(final Exception e) {
            this(e.getMessage(), e.getClass().getName());
        }

        public LambdaError(final String errorMessage, final String errorType) {
            this.errorMessage = errorMessage;
            this.errorType = errorType;
        }
    }

    private static class LambdaContext<E> {
        final CloseableHttpClient http;
        final String runtimeApiHost;
        final BiFunction<E, Context, ?> handler;

        public LambdaContext(final CloseableHttpClient http,
                             final String runtimeApiHost,
                             final BiFunction<E, Context, ?> handler) {
            this.http = http;
            this.runtimeApiHost = runtimeApiHost;
            this.handler = handler;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Lambda.class);

    private static final ObjectMapper jackson = JsonUtils.jackson();

    private Lambda() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static <E> void handleEvents(final BiFunction<E, Context, ?> handler,
                                        final Class<E> type) throws IOException {
        handleEvents(handler, type, System.getenv("AWS_LAMBDA_RUNTIME_API"), HttpClients::createDefault);
    }

    public static <E> void handleEvents(final BiFunction<E, Context, ?> handler,
                                        final Class<E> type,
                                        final String runtimeApiHost,
                                        final Supplier<CloseableHttpClient> httpFactory) throws IOException {
        final String nextEventUrl = eventUrl(runtimeApiHost);
        try (CloseableHttpClient http = httpFactory.get()) {
            final LambdaContext<E> context = new LambdaContext<>(http, runtimeApiHost, handler);
            while (true) {
                handleEvent(context, type, nextEventUrl);
            }
        }
    }

    private static <E> void handleEvent(final LambdaContext<E> lc,
                                        final Class<E> type,
                                        final String url) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");
        try (CloseableHttpResponse response = lc.http.execute(get)) {
            final E event = jackson.readValue(response.getEntity().getContent(), type);
            final long deadlineMs = Long.parseLong(response.getFirstHeader("Lambda-Runtime-Deadline-Ms").getValue());
            final String requestId = response.getFirstHeader("Lambda-Runtime-Aws-Request-Id").getValue();
            final String functionArn = response.getFirstHeader("Lambda-Runtime-Invoked-Function-Arn").getValue();
            final Context context = new DefaultContext(requestId, functionArn, deadlineMs);
            handleEvent(lc, event, context);
        }
    }

    private static <E> void handleEvent(final LambdaContext<E> lc,
                                        final E input,
                                        final Context context) throws IOException {
        try {
            final Object response = lc.handler.apply(input, context);
            handleRequest(lc, context.getAwsRequestId(), response);
        } catch (Exception e) {
            logger.error("Failed to handle request " + context.getAwsRequestId(), e);
            handleRequest(lc, context.getAwsRequestId(), serverError(e));
            handleError(lc, context.getAwsRequestId(), e);
        }
    }

    private static Object serverError(final Exception e) throws IOException {
        return Responses.serverError(jackson.writeValueAsString(new LambdaError(e)));
    }

    private static void handleRequest(final LambdaContext<?> lc,
                                      final String requestId,
                                      final Object response) throws IOException {
        final String responseBody = jackson.writeValueAsString(response);
        sendResponse(lc, responseBody, emptyMap(), responseUrl(lc.runtimeApiHost, requestId));
    }

    private static void handleError(final LambdaContext<?> lc,
                                    final String requestId,
                                    final Exception e) throws IOException {
        final String responseBody = jackson.writeValueAsString(new LambdaError(e));
        sendResponse(lc, responseBody, singletonMap("Lambda-Runtime-Function-Error-Type", "Unhandled"),
                invocationErrorUrl(lc.runtimeApiHost, requestId));
    }

    private static void sendResponse(final LambdaContext<?> lc,
                                     final String responseBody,
                                     final Map<String, String> headers,
                                     final String url) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Language", "en-US");
        post.setHeader("Content-Type", "application/json");
        headers.forEach(post::setHeader);
        post.setEntity(new StringEntity(responseBody));
        try (CloseableHttpResponse response = lc.http.execute(post)) {
            logger.info("Lambda response: {} [{}]",
                    IoUtils.toUtf8String(response.getEntity().getContent()), response.getStatusLine());
        }
    }

    private static String eventUrl(final String host) {
        return String.format("http://%s/2018-06-01/runtime/invocation/next", host);
    }

    private static String responseUrl(final String host, final String requestId) {
        return String.format("http://%s/2018-06-01/runtime/invocation/%s/response", host, requestId);
    }

    private static String invocationErrorUrl(final String host, final String requestId) {
        return String.format("http://%s/2018-06-01/runtime/invocation/%s/error", host, requestId);
    }
}
