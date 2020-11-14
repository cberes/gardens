package gardenmanager.webapp;

import com.amazonaws.services.lambda.runtime.*;

/**
 * {@link Context} implementation for use by Lambda functions compiled by Graal.
 */
class DefaultContext implements Context {
    private final String requestId;
    private final String functionArn;
    private final long deadlineMillis;

    DefaultContext(final String requestId, final String functionArn, final long deadlineMillis) {
        this.requestId = requestId;
        this.functionArn = functionArn;
        this.deadlineMillis = deadlineMillis;
    }

    @Override
    public String getAwsRequestId() {
        return requestId;
    }

    @Override
    public String getInvokedFunctionArn() {
        return functionArn;
    }

    @Override
    public String getFunctionName() {
        return System.getenv("AWS_LAMBDA_FUNCTION_NAME");
    }

    @Override
    public String getFunctionVersion() {
        return System.getenv("AWS_LAMBDA_FUNCTION_VERSION");
    }

    @Override
    public String getLogGroupName() {
        return System.getenv("AWS_LAMBDA_LOG_GROUP_NAME");
    }

    @Override
    public String getLogStreamName() {
        return System.getenv("AWS_LAMBDA_LOG_STREAM_NAME");
    }

    @Override
    public int getMemoryLimitInMB() {
        return Integer.parseInt(System.getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE"));
    }

    @Override
    public int getRemainingTimeInMillis() {
        return (int) Math.max(deadlineMillis - System.currentTimeMillis(), 0);
    }

    @Override
    public LambdaLogger getLogger() {
        return LambdaRuntime.getLogger();
    }

    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        return null;
    }
}
