package gardenmanager.webapp;

import gardenmanager.webapp.plant.ReadGardenListLambda;
import gardenmanager.webapp.gardener.CreateGardenerLambda;
import gardenmanager.webapp.species.DeleteSpeciesLambda;
import gardenmanager.webapp.plant.EditPlantsLambda;
import gardenmanager.webapp.plant.ReadPlantsLambda;
import gardenmanager.webapp.plant.ReadAllPlantsLambda;
import gardenmanager.webapp.util.JsonUtils;

public final class Lambdas {
    public static class LiveReadGardenListLambda extends ReadGardenListLambda {
        public LiveReadGardenListLambda() {
            super(Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveCreateGardenerLambda extends CreateGardenerLambda {
        public LiveCreateGardenerLambda() {
            super(Singletons.gardenerComponent());
        }
    }

    public static class LiveDeleteSpeciesLambda extends DeleteSpeciesLambda {
        public LiveDeleteSpeciesLambda() {
            super(Singletons.speciesComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveEditPlantsLambda extends EditPlantsLambda {
        public LiveEditPlantsLambda() {
            super(JsonUtils.jackson(), Singletons.speciesComponent(), Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveReadPlantsLambda extends ReadPlantsLambda {
        public LiveReadPlantsLambda() {
            super(Singletons.speciesComponent(), Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    public static class LiveReadAllPlantsLambda extends ReadAllPlantsLambda {
        public LiveReadAllPlantsLambda() {
            super(Singletons.speciesComponent(), Singletons.plantComponent(), Singletons.gardenerComponent());
        }
    }

    private Lambdas() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }
}
