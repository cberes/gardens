package gardenmanager.webapp.plant;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import gardenmanager.domain.Plant;
import gardenmanager.plant.PlantRepository;
import gardenmanager.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static gardenmanager.webapp.util.DynamoUtils.s;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class DynamoPlantRepository implements PlantRepository {
    private final String tableName = "GARDEN_PLANT_" + AwsUtils.environmentName();
    private final String plantTableName = "PLANT_" + AwsUtils.environmentName();
    private final DynamoDbClient dynamo;

    public DynamoPlantRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<Plant> findById(final String id) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("ID, GARDENER_ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":id", s(id)))
                .keyConditionExpression("ID = :id")
                .build();
        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoPlantRepository::toPlant).findAny();
    }

    private static Plant toPlant(final Map<String, AttributeValue> item) {
        Plant plant = new Plant();
        plant.setId(item.get("ID").s());
        plant.setGardenerId(item.get("GARDENER_ID").s());
        plant.setGarden(item.get("GARDEN").s());
        plant.setPlanted(ZonedDateTime.parse(item.get("PLANTED").s()));
        return plant;
    }

    @Override
    public List<Plant> findAllByGardenerId(final String gardenerId) {
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
    public List<Plant> findAllByPlantId(final String plantId) {
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

    private List<Plant> findGardensByPlantAndGardener(final String plantId, final String gardenerId) {
        return findAllByGardenerId(gardenerId).stream()
                .filter(plant -> plant.getSpeciesId().equals(plantId))
                .collect(toList());
    }

    @Override
    public List<Plant> findAll() {
        throw new UnsupportedOperationException("why do you need all the garden plants?");
    }

    @Override
    public String save(final Plant plant) {
        if (plant.getId() == null) {
            plant.setPlanted(ZonedDateTime.now(ZoneOffset.UTC));
            plant.setId(id(plant));
            create(plant);
        } else {
            update(plant);
        }
        return plant.getId();
    }

    private static String id(final Plant plant) {
        return String.join(":",
                plant.getGardenerId(),
                plant.getSpeciesId(),
                plant.getGarden().toUpperCase(Locale.US));
    }

    private void create(final Plant plant) {
        final PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(toAttrMap(plant))
                .build();
        dynamo.putItem(request);
    }

    private static Map<String, AttributeValue> toAttrMap(final Plant plant) {
        return Map.of(
                "ID", s(plant.getId()),
                "GARDENER_ID", s(plant.getGardenerId()),
                "GARDEN", s(plant.getGarden()),
                "PLANTED", s(plant.getPlanted().toString()));
    }

    private void update(final Plant plant) {
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
    public void delete(final Plant plant) {
        final DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(plant.getId()),
                        "GARDENER_ID", s(plant.getGardenerId())))
                .build();
        dynamo.deleteItem(request);
    }
}
