package gardenmanager.webapp.plant;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import gardenmanager.domain.Plant;
import gardenmanager.plant.PlantRepository;
import gardenmanager.webapp.species.PlantId;
import gardenmanager.webapp.species.SpeciesId;
import gardenmanager.webapp.util.Tables;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static gardenmanager.webapp.util.DynamoUtils.s;
import static java.util.stream.Collectors.toList;

public class DynamoPlantRepository implements PlantRepository {
    private final String tableName = Tables.plant();
    private final DynamoDbClient dynamo;

    public DynamoPlantRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<Plant> findById(final String compositeId) {
        final PlantId id = PlantId.fromString(compositeId);
        final SpeciesId speciesId = SpeciesId.fromString(id.getSpeciesId());
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("GARDENER_ID, ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(
                        ":gardenerId", s(speciesId.getGardenerId()),
                        ":id", s(compositeId)))
                .keyConditionExpression("GARDENER_ID = :gardenerId and ID = :id")
                .build();
        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoPlantRepository::toPlant).findAny();
    }

    private static Plant toPlant(final Map<String, AttributeValue> item) {
        Plant plant = new Plant();
        plant.setGarden(item.get("GARDEN").s());
        plant.setPlanted(ZonedDateTime.parse(item.get("PLANTED").s()));
        plant.setGardenerId(item.get("GARDENER_ID").s());
        final PlantId id = PlantId.fromString(item.get("ID").s());
        plant.setSpeciesId(id.getSpeciesId());
        plant.setId(item.get("ID").s());
        return plant;
    }

    @Override
    public List<Plant> findAllByGardenerId(final String gardenerId) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("GARDENER_ID, ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":gardenerId", s(gardenerId)))
                .keyConditionExpression("GARDENER_ID = :gardenerId")
                .build();

        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoPlantRepository::toPlant).collect(toList());
    }

    @Override
    public List<Plant> findAllBySpeciesId(final String speciesId) {
        final SpeciesId id = SpeciesId.fromString(speciesId);
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("GARDENER_ID, ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(
                        ":gardenerId", s(id.getGardenerId()),
                        ":speciesId", s(new PlantId(speciesId, "").toString())))
                .keyConditionExpression("GARDENER_ID = :gardenerId and begins_with(ID, :speciesId)")
                .build();

        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoPlantRepository::toPlant).collect(toList());
    }

    @Override
    public List<Plant> findAll() {
        throw new UnsupportedOperationException("why do you need all the garden plants?");
    }

    @Override
    public String save(final Plant plant) {
        if (plant.getId() == null) {
            plant.setPlanted(ZonedDateTime.now(ZoneOffset.UTC));
            plant.setId(createId(plant));
            create(plant);
        } else {
            update(plant);
        }
        return plant.getId();
    }

    private static String createId(final Plant plant) {
        return new PlantId(plant.getSpeciesId(), UUID.randomUUID().toString()).toString();
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
                "GARDENER_ID", s(plant.getGardenerId()),
                "ID", s(plant.getId()),
                "GARDEN", s(plant.getGarden()),
                "PLANTED", s(plant.getPlanted().toString()));
    }

    private void update(final Plant plant) {
        final UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "GARDENER_ID", s(plant.getGardenerId()),
                        "ID", s(plant.getId())))
                .expressionAttributeValues(Map.of(
                        ":garden", s(plant.getGarden())))
                .updateExpression("set GARDEN = :garden")
                .build();
        dynamo.updateItem(request);
    }

    @Override
    public void delete(final Plant plant) {
        final DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "GARDENER_ID", s(plant.getGardenerId()),
                        "ID", s(plant.getId())))
                .build();
        dynamo.deleteItem(request);
    }
}
