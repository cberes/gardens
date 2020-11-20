package gardenmanager.webapp.dynamo.tables;

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

interface DynamoOps {
    default AttributeDefinition attribute(final String name, final ScalarAttributeType type) {
        return AttributeDefinition.builder()
                .attributeName(name)
                .attributeType(type)
                .build();
    }

    default KeySchemaElement key(final String name, final KeyType type) {
        return KeySchemaElement.builder()
                .attributeName(name)
                .keyType(type)
                .build();
    }
}
