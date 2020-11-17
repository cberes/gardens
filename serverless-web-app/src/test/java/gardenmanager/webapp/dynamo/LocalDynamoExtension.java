package gardenmanager.webapp.dynamo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.almworks.sqlite4java.SQLite;
import gardenmanager.webapp.dynamo.tables.GardenerTable;
import gardenmanager.webapp.dynamo.tables.PlantTable;
import gardenmanager.webapp.dynamo.tables.SpeciesTable;
import org.junit.jupiter.api.extension.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;

import static java.util.stream.Collectors.toMap;

public class LocalDynamoExtension implements TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, ExtensionContext.Store.CloseableResource {
    private volatile LocalDynamoDb localDynamo;

    private final Map<String, DynamoTable> tablesByName = Stream.of(
            new GardenerTable(),
            new SpeciesTable(),
            new PlantTable()
    ).collect(toMap(DynamoTable::tableName, x -> x));

    @Override
    public void postProcessTestInstance(final Object instance, final ExtensionContext context) throws Exception {
        if (localDynamo == null) {
            SQLite.setLibraryPath(System.getenv("NATIVE_LIBS_DIR"));
            localDynamo = new LocalDynamoDb();
            localDynamo.start();
            context.getStore(ExtensionContext.Namespace.GLOBAL).put("local_dynamo", this);
        }

        setDynamo(localDynamo.createClient(), instance);
    }

    private void setDynamo(final DynamoDbClient client, final Object instance) {
        Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(InjectDynamo.class))
                .findFirst()
                .ifPresent(field -> {
                    field.setAccessible(true);

                    try {
                        field.set(instance, client);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        if (localDynamo != null) {
            context.getTestInstance().ifPresent(
                    instance -> createTables(localDynamo.createClient(), instance));
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        if (localDynamo != null) {
            context.getTestInstance().ifPresent(
                    instance -> deleteTables(localDynamo.createClient(), instance));
        }
    }

    private void createTables(final DynamoDbClient client, final Object instance) {
        final Set<String> tableNames = tableNames(client);
        UseTables[] uses = instance.getClass().getAnnotationsByType(UseTables.class);
        for (UseTables use : uses) {
            for (String tableName : use.value()) {
                DynamoTable table = tablesByName.get(tableName);
                if (!tableNames.contains(table.tableName())) {
                    table.createTable(client);
                }
            }
        }
    }

    private Set<String> tableNames(final DynamoDbClient dynamo) {
        return new HashSet<>(dynamo.listTables().tableNames());
    }

    private void deleteTables(final DynamoDbClient client, final Object instance) {
        final Set<String> tableNames = tableNames(client);
        UseTables[] uses = instance.getClass().getAnnotationsByType(UseTables.class);
        for (UseTables use : uses) {
            for (String tableName : use.value()) {
                if (tableNames.contains(tableName)) {
                    client.deleteTable(DeleteTableRequest.builder()
                            .tableName(tableName)
                            .build());
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (localDynamo != null) {
            localDynamo.stop();
        }
    }
}
