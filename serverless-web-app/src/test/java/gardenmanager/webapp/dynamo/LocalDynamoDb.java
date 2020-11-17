package gardenmanager.webapp.dynamo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Adapted from AWS SDK GitHub.
 * See https://github.com/aws/aws-sdk-java-v2/blob/93269d4c0416d0f72e086774265847d6af0d54ec/services-custom/dynamodb-enhanced/src/test/java/software/amazon/awssdk/extensions/dynamodb/mappingclient/functionaltests/LocalDynamoDb.java
 *
 * Here's what they had to say about it:
 * Wrapper for a local DynamoDb server used in testing. Each instance of this class will find a new port to run on,
 * so multiple instances can be safely run simultaneously. Each instance of this service uses memory as a storage medium
 * and is thus completely ephemeral; no data will be persisted between stops and starts.
 *
 * LocalDynamoDb localDynamoDb = new LocalDynamoDb();
 * localDynamoDb.start();       // Start the service running locally on host
 * DynamoDbClient dynamoDbClient = localDynamoDb.createClient();
 * ...      // Do your testing with the client
 * localDynamoDb.stop();        // Stop the service and free up resources
 *
 * If possible it's recommended to keep a single running instance for all your tests, as it can be slow to teardown
 * and create new servers for every test, but there have been observed problems when dropping tables between tests for
 * this scenario, so it's best to write your tests to be resilient to tables that already have data in them.
 */
class LocalDynamoDb {
    private DynamoDBProxyServer server;
    private int port;
    private String endpoint;

    /**
     * Start the local DynamoDb service and run in background
     */
    void start() throws Exception {
        port = getFreePort();
        endpoint = String.format("http://localhost:%d", port);
        server = createServer();
        server.start();
    }

    private static int getFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    private DynamoDBProxyServer createServer() throws Exception {
        return ServerRunner.createServerFromCommandLineArgs(
                new String[] {  "-inMemory", "-port", Integer.toString(port) });
    }

    /**
     * Create a standard AWS v2 SDK client pointing to the local DynamoDb instance
     * @return A DynamoDbClient pointing to the local DynamoDb instance
     */
    DynamoDbClient createClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                // The region is meaningless for local DynamoDb but required for client builder validation
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummy-key", "dummy-secret")))
                .build();
    }

    /**
     * Stops the local DynamoDb service and frees up resources it is using.
     */
    void stop() throws Exception {
        server.stop();
    }
}