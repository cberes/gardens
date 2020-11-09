package com.ohboywerecamping.webapp.garden;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import com.ohboywerecamping.domain.GardenPlant;
import com.ohboywerecamping.garden.GardenRepository;
import com.ohboywerecamping.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static com.ohboywerecamping.webapp.util.DynamoUtils.s;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class DynamoGardenRepository implements GardenRepository {
    private final String tableName = "GARDEN_PLANT_" + AwsUtils.environmentName();
    private final String plantTableName = "PLANT_" + AwsUtils.environmentName();
    private final DynamoDbClient dynamo;

    public DynamoGardenRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<GardenPlant> findById(final String id) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("ID, GARDENER_ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":id", s(id)))
                .keyConditionExpression("ID = :id")
                .build();
        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoGardenRepository::toPlant).findAny();
    }

    private static GardenPlant toPlant(final Map<String, AttributeValue> item) {
        GardenPlant plant = new GardenPlant();
        plant.setId(item.get("ID").s());
        plant.setGardenerId(item.get("GARDENER_ID").s());
        plant.setGarden(item.get("GARDEN").s());
        plant.setPlanted(ZonedDateTime.parse(item.get("PLANTED").s()));
        return plant;
    }

    @Override
    public List<GardenPlant> findAllByGardenerId(final String gardenerId) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName("GARDENS_BY_GARDENER_ID")
                .projectionExpression("ID")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":gardener_id", s(gardenerId)))
                .keyConditionExpression("GARDENER_ID = :gardener_id")
                .build();

        final QueryResponse response = dynamo.query(request);
        return response.items().stream()
                .map(item -> item.get("ID").s())
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    @Override
    public List<GardenPlant> findAllByPlantId(final String plantId) {
        // Query the plant table to get the gardener ID, then get plants by gardener, and filter by plant.
        // It removes the need for an index. TODO is this worth it?
        final QueryRequest request = QueryRequest.builder()
                .tableName(plantTableName)
                .projectionExpression("GARDENER_ID")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":id", s(plantId)))
                .keyConditionExpression("ID = :id")
                .build();

        final QueryResponse response = dynamo.query(request);
        return response.items().stream()
                .map(item -> item.get("GARDENER_ID").s())
                .findAny()
                .map(gardenerId -> findGardensByPlantAndGardener(plantId, gardenerId))
                .orElse(emptyList());
    }

    private List<GardenPlant> findGardensByPlantAndGardener(final String plantId, final String gardenerId) {
        return findAllByGardenerId(gardenerId).stream()
                .filter(plant -> plant.getPlantId().equals(plantId))
                .collect(toList());
    }

    @Override
    public List<GardenPlant> findAll() {
        throw new UnsupportedOperationException("why do you need all the garden plants?");
    }

    @Override
    public String save(final GardenPlant plant) {
        if (plant.getId() == null) {
            plant.setPlanted(ZonedDateTime.now(ZoneOffset.UTC));
            plant.setId(id(plant));
            create(plant);
        } else {
            update(plant);
        }
        return plant.getId();
    }

    private static String id(final GardenPlant plant) {
        return String.join(":",
                plant.getGardenerId(),
                plant.getPlantId(),
                plant.getGarden().toUpperCase(Locale.US));
    }

    private void create(final GardenPlant plant) {
        final PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(toAttrMap(plant))
                .build();
        dynamo.putItem(request);
    }

    private static Map<String, AttributeValue> toAttrMap(final GardenPlant plant) {
        return Map.of(
                "ID", s(plant.getId()),
                "GARDENER_ID", s(plant.getGardenerId()),
                "GARDEN", s(plant.getGarden()),
                "PLANTED", s(plant.getPlanted().toString()));
    }

    private void update(final GardenPlant plant) {
        final var attrMap = toAttrMap(plant);
        final UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(plant.getId()),
                        "GARDENER_ID", s(plant.getGardenerId())))
                .expressionAttributeValues(Map.of(
                        ":garden", attrMap.get("GARDEN")))
                .updateExpression("set GARDEN = :garden")
                .build();
        dynamo.updateItem(request);
    }

    @Override
    public void delete(final GardenPlant plant) {
        final DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(plant.getId()),
                        "GARDENER_ID", s(plant.getGardenerId())))
                .build();
        dynamo.deleteItem(request);
    }
}
