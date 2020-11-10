package gardenmanager.webapp;

import gardenmanager.webapp.garden.ReadGardenListLambda;
import gardenmanager.webapp.gardener.CreateGardenerLambda;
import gardenmanager.webapp.plant.DeletePlantLambda;
import gardenmanager.webapp.plant.EditPlantLambda;
import gardenmanager.webapp.plant.ReadPlantLambda;
import gardenmanager.webapp.plant.ReadPlantListLambda;
import gardenmanager.webapp.util.JsonUtils;

public final class Lambdas {
    public static class LiveReadGardenListLambda extends ReadGardenListLambda {
        public LiveReadGardenListLambda() {
            super(Singletons.gardenComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveCreateGardenerLambda extends CreateGardenerLambda {
        public LiveCreateGardenerLambda() {
            super(Singletons.gardenerComponent());
        }
    }

    public static class LiveDeletePlantLambda extends DeletePlantLambda {
        public LiveDeletePlantLambda() {
            super(Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveEditPlantLambda extends EditPlantLambda {
        public LiveEditPlantLambda() {
            super(JsonUtils.jackson(), Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveReadPlantLambda extends ReadPlantLambda {
        public LiveReadPlantLambda() {
            super(Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveReadPlantListLambda extends ReadPlantListLambda {
        public LiveReadPlantListLambda() {
            super(Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    private Lambdas() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }
}
