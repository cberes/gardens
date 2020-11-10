package gardenmanager.webapp.species;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import gardenmanager.domain.LightPreference;
import gardenmanager.domain.MoisturePreference;
import gardenmanager.domain.Species;
import gardenmanager.species.SpeciesRepository;
import gardenmanager.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import static gardenmanager.webapp.util.DynamoUtils.s;
import static java.util.stream.Collectors.toList;

public class DynamoSpeciesRepository implements SpeciesRepository {
    private final String tableName = "PLANT_" + AwsUtils.environmentName();
    private final DynamoDbClient dynamo;

    public DynamoSpeciesRepository(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Optional<Species> findById(final String id) {
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
        return response.items().stream().map(DynamoSpeciesRepository::toPlant).findAny();
    }

    private static Species toPlant(final Map<String, AttributeValue> item) {
        Species species = new Species();
        species.setId(item.get("ID").s());
        species.setGardenerId(item.get("GARDENER_ID").s());
        species.setName(item.get("NAME").s());
        species.setAlternateName(item.get("NAME_ALT").s());
        species.setLight(LightPreference.valueOf(item.get("LIGHT").s()));
        species.setMoisture(MoisturePreference.valueOf(item.get("MOISTURE").s()));
        return species;
    }

    @Override
    public List<Species> findAllByGardenerId(final String gardenerId) {
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
    public List<Species> findAll() {
        throw new UnsupportedOperationException("why do you need all the plants?");
    }

    @Override
    public String save(final Species species) {
        if (species.getId() == null) {
            species.setId(UUID.randomUUID().toString());
            create(species);
        } else {
            update(species);
        }
        return species.getId();
    }

    private void create(final Species species) {
        final PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(toAttrMap(species))
                .expressionAttributeNames(Map.of(
                        "#name", "NAME"))
                .build();
        dynamo.putItem(request);
    }

    private static Map<String, AttributeValue> toAttrMap(final Species species) {
        return Map.of(
                "ID", s(species.getId()),
                "GARDENER_ID", s(species.getGardenerId()),
                "#name", s(species.getName()),
                "NAME_ALT", s(species.getAlternateName()),
                "LIGHT", s(species.getLight().name()),
                "MOISTURE", s(species.getMoisture().name()));
    }

    private void update(final Species species) {
        final var attrMap = toAttrMap(species);
        final UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(species.getId()),
                        "GARDENER_ID", s(species.getGardenerId())))
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
    public void delete(final Species species) {
        final DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "ID", s(species.getId()),
                        "GARDENER_ID", s(species.getGardenerId())))
                .build();
        dynamo.deleteItem(request);
    }
}