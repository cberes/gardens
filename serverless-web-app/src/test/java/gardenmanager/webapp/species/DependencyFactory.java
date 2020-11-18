package gardenmanager.webapp.species;

import gardenmanager.gardener.GardenerComponent;
import gardenmanager.gardener.GardenerComponentImpl;
import gardenmanager.gardener.GardenerRepository;
import gardenmanager.plant.PlantComponent;
import gardenmanager.plant.PlantComponentImpl;
import gardenmanager.plant.PlantRepository;
import gardenmanager.species.SpeciesComponent;
import gardenmanager.species.SpeciesComponentImpl;
import gardenmanager.species.SpeciesRepository;
import gardenmanager.webapp.gardener.DynamoGardenerRepository;
import gardenmanager.webapp.plant.DynamoPlantRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DependencyFactory {
    private final DynamoDbClient dynamo;
    private GardenerRepository gardenerRepo;
    private GardenerComponent gardenerComp;
    private SpeciesRepository speciesRepo;
    private SpeciesComponent speciesComp;
    private PlantRepository plantRepo;
    private PlantComponent plantComp;

    public DependencyFactory(final DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    public GardenerRepository gardenerRepo() {
        if (gardenerRepo == null) {
            gardenerRepo = new DynamoGardenerRepository(dynamo);
        }
        return gardenerRepo;
    }

    public GardenerComponent gardenerComp() {
        if (gardenerComp == null) {
            gardenerComp = new GardenerComponentImpl(gardenerRepo());
        }
        return gardenerComp;
    }

    public SpeciesRepository speciesRepo() {
        if (speciesRepo == null) {
            speciesRepo = new DynamoSpeciesRepository(dynamo);
        }
        return speciesRepo;
    }

    public SpeciesComponent speciesComp() {
        if (speciesComp == null) {
            speciesComp = new SpeciesComponentImpl(speciesRepo());
        }
        return speciesComp;
    }

    public PlantRepository plantRepo() {
        if (plantRepo == null) {
            plantRepo = new DynamoPlantRepository(dynamo);
        }
        return plantRepo;
    }

    public PlantComponent plantComp() {
        if (plantComp == null) {
            plantComp = new PlantComponentImpl(plantRepo(), speciesRepo());
        }
        return plantComp;
    }
}
