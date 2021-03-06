package gardenmanager.webapp;

import gardenmanager.plant.PlantComponent;
import gardenmanager.plant.PlantComponentImpl;
import gardenmanager.plant.PlantRepository;
import gardenmanager.gardener.GardenerComponent;
import gardenmanager.gardener.GardenerComponentImpl;
import gardenmanager.gardener.GardenerRepository;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.species.SpeciesComponentImpl;
import gardenmanager.species.SpeciesRepository;
import gardenmanager.webapp.plant.DynamoPlantRepository;
import gardenmanager.webapp.gardener.DynamoGardenerRepository;
import gardenmanager.webapp.species.DynamoSpeciesRepository;
import gardenmanager.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Instances of all Lambda function dependencies.
 */
final class Singletons {
    private static final Singletons instance = new Singletons();

    private DynamoDbClient ddb;
    private PlantRepository plants;
    private GardenerRepository gardeners;
    private SpeciesRepository species;
    private PlantComponent plantComponent;
    private GardenerComponent gardenerComponent;
    private SpeciesComponent speciesComponent;

    private Singletons() {
    }

    static DynamoDbClient dynamo() {
        if (instance.ddb == null) {
            instance.ddb = DynamoDbClient.builder().region(AwsUtils.region()).build();
        }
        return instance.ddb;
    }

    static PlantRepository plants() {
        if (instance.plants == null) {
            instance.plants = new DynamoPlantRepository(dynamo());
        }
        return instance.plants;
    }

    static GardenerRepository gardeners() {
        if (instance.gardeners == null) {
            instance.gardeners = new DynamoGardenerRepository(dynamo());
        }
        return instance.gardeners;
    }

    static SpeciesRepository species() {
        if (instance.species == null) {
            instance.species = new DynamoSpeciesRepository(dynamo());
        }
        return instance.species;
    }

    static PlantComponent plantComponent() {
        if (instance.plantComponent == null) {
            instance.plantComponent = new PlantComponentImpl(plants(), species());
        }
        return instance.plantComponent;
    }

    static GardenerComponent gardenerComponent() {
        if (instance.gardenerComponent == null) {
            instance.gardenerComponent = new GardenerComponentImpl(gardeners());
        }
        return instance.gardenerComponent;
    }

    static SpeciesComponent speciesComponent() {
        if (instance.speciesComponent == null) {
            instance.speciesComponent = new SpeciesComponentImpl(species());
        }
        return instance.speciesComponent;
    }
}
