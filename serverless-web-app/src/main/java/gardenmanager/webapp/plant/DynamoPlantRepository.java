package gardenmanager.webapp.plant;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import gardenmanager.domain.LightPreference;
import gardenmanager.domain.MoisturePreference;
import gardenmanager.domain.Plant;
import gardenmanager.plant.PlantRepository;
import gardenmanager.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static gardenmanager.webapp.util.DynamoUtils.s;
import static java.util.stream.Collectors.toList;

public class DynamoPlantRepository implements PlantRepository {
    private final String tableName = "PLANT_" + AwsUtils.environmentName();
    private final DynamoDbClient dynamo;

    public DynamoPlantRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<Plant> findById(final String id) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("ID, GARDENER_ID, #name, NAME_ALT, LIGHT, MOISTURE")
                .consistentRead(false)
                .expressionAttributeNames(Map.of(
                        "#name", "NAME"))
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
        plant.setName(item.get("NAME").s());
        plant.setAlternateName(item.get("NAME_ALT").s());
        plant.setLight(LightPreference.valueOf(item.get("LIGHT").s()));
        plant.setMoisture(MoisturePreference.valueOf(item.get("MOISTURE").s()));
        return plant;
    }

    @Override
    public List<Plant> findAllByGardenerId(final String gardenerId) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName("PLANTS_BY_GARDENER_ID")
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
                .distinct()
                .collect(toList());
    }

    @Override
    public List<Plant> findAll() {
        throw new UnsupportedOperationException("why do you need all the plants?");
    }

    @Override
    public String save(final Plant plant) {
        if (plant.getId() == null) {
            plant.setId(UUID.randomUUID().toString());
            create(plant);
        } else {
            update(plant);
        }
        return plant.getId();
    }

    private void create(final Plant plant) {
        final PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(toAttrMap(plant))
                .expressionAttributeNames(Map.of(
                        "#name", "NAME"))
                .build();
        dynamo.putItem(request);
    }

    private static Map<String, AttributeValue> toAttrMap(final Plant plant) {
        return Map.of(
                "ID", s(plant.getId()),
                "GARDENER_ID", s(plant.getGardenerId()),
                "#name", s(plant.getName()),
                "NAME_ALT", s(plant.getAlternateName()),
                "LIGHT", s(plant.getLight().name()),
                "MOISTURE", s(plant.getMoisture().name()));
    }

    private void update(final Plant plant) {
        final var attrMap = toAttrMap(plant);
        final UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(plant.getId()),
                        "GARDENER_ID", s(plant.getGardenerId())))
                .expressionAttributeNames(Map.of(
                        "#name", "NAME"))
                .expressionAttributeValues(Map.of(
                        ":name", attrMap.get("#name"),
                        ":nameAlt", attrMap.get("NAME_ALT"),
                        ":light", attrMap.get("LIGHT"),
                        ":moisture", attrMap.get("MOISTURE")))
                .updateExpression("set #name = :name, NAME_ALT = :nameAlt, LIGHT = :light, MOISTURE = :moisture")
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
