package gardenmanager.webapp.dynamo.tables;

import gardenmanager.webapp.dynamo.DynamoTable;
import gardenmanager.webapp.util.Tables;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class GardenerTable implements DynamoTable, DynamoOps {
    @Override
    public String tableName() {
        return Tables.gardener();
    }

    @Override
    public void createTable(final DynamoDbClient dynamo) {
        dynamo.createTable(CreateTableRequest.builder()
                .tableName(Tables.gardener())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(attribute("ID", ScalarAttributeType.S))
                .keySchema(key("ID", KeyType.HASH))
                .build());
    }
}
