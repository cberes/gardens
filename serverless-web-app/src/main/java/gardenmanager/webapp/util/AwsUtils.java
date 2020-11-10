package gardenmanager.webapp.util;

import software.amazon.awssdk.regions.Region;

public final class AwsUtils {
    private AwsUtils() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static Region region() {
        return Region.of(System.getenv("AWS_REGION"));
    }

    public static String environmentName() {
        return System.getenv("APP_ENV_NAME");
    }
}
