package com.ohboywerecamping.webapp;

import com.ohboywerecamping.garden.GardenComponent;
import com.ohboywerecamping.garden.GardenComponentImpl;
import com.ohboywerecamping.garden.GardenRepository;
import com.ohboywerecamping.gardener.GardenerComponent;
import com.ohboywerecamping.gardener.GardenerComponentImpl;
import com.ohboywerecamping.gardener.GardenerRepository;
import com.ohboywerecamping.plant.PlantComponent;
import com.ohboywerecamping.plant.PlantComponentImpl;
import com.ohboywerecamping.plant.PlantRepository;
import com.ohboywerecamping.webapp.garden.DynamoGardenRepository;
import com.ohboywerecamping.webapp.gardener.DynamoGardenerRepository;
import com.ohboywerecamping.webapp.plant.DynamoPlantRepository;
import com.ohboywerecamping.webapp.util.AwsUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

final class Singletons {
    private static final Singletons instance = new Singletons();

    private DynamoDbClient ddb;
    private GardenRepository gardens;
    private GardenerRepository gardeners;
    private PlantRepository plants;
    private GardenComponent gardenComponent;
    private GardenerComponent gardenerComponent;
    private PlantComponent plantComponent;

    private Singletons() {
    }

    static DynamoDbClient dynamo() {
        if (instance.ddb == null) {
            instance.ddb = DynamoDbClient.builder().region(AwsUtils.region()).build();
        }
        return instance.ddb;
    }

    static GardenRepository gardens() {
        if (instance.gardens == null) {
            instance.gardens = new DynamoGardenRepository(dynamo());
        }
        return instance.gardens;
    }

    static GardenerRepository gardeners() {
        if (instance.gardeners == null) {
            instance.gardeners = new DynamoGardenerRepository(dynamo());
        }
        return instance.gardeners;
    }

    static PlantRepository plants() {
        if (instance.plants == null) {
            instance.plants = new DynamoPlantRepository(dynamo());
        }
        return instance.plants;
    }

    static GardenComponent gardenComponent() {
        if (instance.gardenComponent == null) {
            instance.gardenComponent = new GardenComponentImpl(gardens());
        }
        return instance.gardenComponent;
    }

    static GardenerComponent gardenerComponent() {
        if (instance.gardenerComponent == null) {
            instance.gardenerComponent = new GardenerComponentImpl(gardeners());
        }
        return instance.gardenerComponent;
    }

    static PlantComponent plantComponent() {
        if (instance.plantComponent == null) {
            instance.plantComponent = new PlantComponentImpl(plants(), gardens());
        }
        return instance.plantComponent;
    }
}
