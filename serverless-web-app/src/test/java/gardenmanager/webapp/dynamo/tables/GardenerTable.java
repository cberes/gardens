package gardenmanager.webapp.dynamo.tables;

import gardenmanager.webapp.dynamo.DynamoTable;
import gardenmanager.webapp.util.Tables;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class GardenerTable implements DynamoTable {
    @Override
    public String tableName() {
        return Tables.gardener();
    }

    @Override
    public void createTable(final DynamoDbClient dynamo) {
        dynamo.createTable(CreateTableRequest.builder()
                .tableName(Tables.gardener())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("ID")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName("ID")
                        .keyType(KeyType.HASH)
                        .build())
                .build());
    }
}
