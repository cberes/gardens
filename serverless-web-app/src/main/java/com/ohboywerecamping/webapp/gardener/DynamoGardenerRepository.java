package com.ohboywerecamping.webapp.gardener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import java.util.Map;
import java.util.Optional;

import com.ohboywerecamping.domain.Gardener;
import com.ohboywerecamping.gardener.GardenerRepository;
import com.ohboywerecamping.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static com.ohboywerecamping.webapp.util.DynamoUtils.s;

public class DynamoGardenerRepository implements GardenerRepository {
    private final String tableName = "GARDENER_" + AwsUtils.environmentName();
    private final DynamoDbClient dynamo;

    public DynamoGardenerRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<Gardener> findById(final String id) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("ID, EMAIL, JOINED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":id", s(id)))
                .keyConditionExpression("ID = :id")
                .build();
        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoGardenerRepository::toGardener).findAny();
    }

    private static Gardener toGardener(final Map<String, AttributeValue> item) {
        Gardener gardener = new Gardener();
        gardener.setId(item.get("ID").s());
        gardener.setEmail(item.get("EMAIL").s());
        gardener.setJoined(ZonedDateTime.parse(item.get("JOINED").s()));
        return gardener;
    }

    @Override
    public Optional<Gardener> findByEmail(final String email) {
        return findById(email);
    }

    @Override
    public List<Gardener> findAll() {
        throw new UnsupportedOperationException("why do you need all the gardeners?");
    }

    @Override
    public String save(final Gardener gardener) {
        if (gardener.getId() == null) {
            gardener.setJoined(ZonedDateTime.now(ZoneOffset.UTC));
            gardener.setId(gardener.getEmail());
            create(gardener);
        } else {
            // TODO what if user wants to change his email?
            throw new IllegalArgumentException("gardener cannot be updated");
        }
        return gardener.getId();
    }

    private void create(final Gardener gardener) {
        final PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(toAttrMap(gardener))
                .build();
        dynamo.putItem(request);
    }

    private static Map<String, AttributeValue> toAttrMap(final Gardener gardener) {
        return Map.of(
                "ID", s(gardener.getId()),
                "EMAIL", s(gardener.getEmail()),
                "JOINED", s(gardener.getJoined().toString()));
    }

    @Override
    public void delete(final Gardener gardener) {
        final DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(gardener.getId())))
                .build();
        dynamo.deleteItem(request);
    }
}
