package gardenmanager.webapp.dynamo.tables;

import gardenmanager.webapp.dynamo.DynamoTable;
import gardenmanager.webapp.util.Tables;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class PlantTable implements DynamoTable, DynamoOps {
    @Override
    public String tableName() {
        return Tables.plant();
    }

    @Override
    public void createTable(final DynamoDbClient dynamo) {
        dynamo.createTable(CreateTableRequest.builder()
                .tableName(Tables.plant())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        attribute("GARDENER_ID", ScalarAttributeType.S),
                        attribute("ID", ScalarAttributeType.S))
                .keySchema(
                        key("GARDENER_ID", KeyType.HASH),
                        key("ID", KeyType.RANGE))
                .build());
    }
}
