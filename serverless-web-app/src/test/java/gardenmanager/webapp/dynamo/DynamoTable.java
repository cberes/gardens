package gardenmanager.webapp.dynamo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public interface DynamoTable {
    String tableName();

    void createTable(DynamoDbClient dynamo);
}
