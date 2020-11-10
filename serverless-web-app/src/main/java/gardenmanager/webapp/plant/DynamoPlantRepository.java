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
import static java.util.stream.Collectors.toList;

public class DynamoPlantRepository implements PlantRepository {
    private final String tableName = "PLANT_" + AwsUtils.environmentName();
    private final String speciesTableName = "SPECIES_" + AwsUtils.environmentName();
    private final DynamoDbClient dynamo;

    public DynamoPlantRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<Plant> findById(final String compositeId) {
        final Id id = Id.fromString(compositeId);
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("GARDENER_SPECIES_ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(
                        ":id", s(compositeId),
                        ":garden", s(id.getGarden())))
                .keyConditionExpression("GARDENER_SPECIES_ID = :id and GARDEN = :garden")
                .build();
        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(DynamoPlantRepository::toPlant).findAny();
    }

    private static Plant toPlant(final Map<String, AttributeValue> item) {
        Plant plant = new Plant();
        plant.setGarden(item.get("GARDEN").s());
        plant.setPlanted(ZonedDateTime.parse(item.get("PLANTED").s()));
        final gardenmanager.webapp.species.Id id =
                gardenmanager.webapp.species.Id.fromString(item.get("GARDENER_SPECIES_ID").s());
        plant.setGardenerId(id.getGardenerId());
        plant.setSpeciesId(id.getSpeciesId());
        plant.setId(id(plant));
        return plant;
    }

    @Override
    public List<Plant> findAllByGardenerId(final String gardenerId) {
        // query the species table for all species belonging to the gardener
        // to avoid the need for an index TODO is it worth it?
        return findAllSpeciesIdsByGardenerId(gardenerId)
                .map(speciesId -> new gardenmanager.webapp.species.Id(gardenerId, speciesId).toString())
                .parallel() // TODO does the parallel stream help?
                .flatMap(speciesId -> findAllBySpeciesId(speciesId).stream())
                .collect(toList());
    }

    private java.util.stream.Stream<String> findAllSpeciesIdsByGardenerId(final String gardenerId) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(speciesTableName)
                .projectionExpression("SPECIES_ID")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":gardener_id", s(gardenerId)))
                .keyConditionExpression("GARDENER_ID = :gardener_id")
                .build();

        final QueryResponse response = dynamo.query(request);
        return response.items().stream().map(item -> item.get("SPECIES_ID").s());
    }

    @Override
    public List<Plant> findAllBySpeciesId(final String speciesId) {
        final QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .projectionExpression("GARDENER_SPECIES_ID, GARDEN, PLANTED")
                .consistentRead(false)
                .expressionAttributeValues(Map.of(":gsId", s(speciesId)))
                .keyConditionExpression("GARDENER_SPECIES_ID = :gsId")
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
            plant.setId(id(plant));
            create(plant);
        } else {
            update(plant);
        }
        return plant.getId();
    }

    private static String id(final Plant plant) {
        return new Id(plant.getGardenerId(), plant.getSpeciesId(), plant.getGarden()).toString();
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
                "GARDENER_SPECIES_ID", s(parentId(plant)),
                "GARDEN", s(plant.getGarden()),
                "PLANTED", s(plant.getPlanted().toString()));
    }

    private static String parentId(final Plant plant) {
        return new gardenmanager.webapp.species.Id(plant.getGardenerId(), plant.getSpeciesId()).toString();
    }

    private void update(final Plant plant) {
        final Id id = Id.fromString(plant.getId());
        final UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "GARDENER_SPECIES_ID", s(parentId(plant)),
                        "GARDEN", s(id.getGarden())))
                .expressionAttributeValues(Map.of(
                        ":garden", s(plant.getGarden())))
                .updateExpression("set GARDEN = :garden")
                .build();
        dynamo.updateItem(request);
    }

    @Override
    public void delete(final Plant plant) {
        final Id id = Id.fromString(plant.getId());
        final DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "GARDENER_SPECIES_ID", s(parentId(plant)),
                        "GARDEN", s(id.getGarden())))
                .build();
        dynamo.deleteItem(request);
    }
}
