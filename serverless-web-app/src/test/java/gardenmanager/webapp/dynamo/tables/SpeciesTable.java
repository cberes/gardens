package gardenmanager.webapp.dynamo.tables;

import gardenmanager.webapp.dynamo.DynamoTable;
import gardenmanager.webapp.util.Tables;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class SpeciesTable implements DynamoTable {
    @Override
    public String tableName() {
        return Tables.species();
    }

    @Override
    public void createTable(final DynamoDbClient dynamo) {
        dynamo.createTable(CreateTableRequest.builder()
                .tableName(Tables.species())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(AttributeDefinition.builder()
                                .attributeName("GARDENER_ID")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("ID")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .keySchema(KeySchemaElement.builder()
                                .attributeName("GARDENER_ID")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("ID")
                                .keyType(KeyType.RANGE)
                                .build())
                .build());
    }
}
