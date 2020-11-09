package com.ohboywerecamping.webapp;

import com.ohboywerecamping.webapp.garden.ReadGardenListLambda;
import com.ohboywerecamping.webapp.gardener.CreateGardenerLambda;
import com.ohboywerecamping.webapp.plant.DeletePlantLambda;
import com.ohboywerecamping.webapp.plant.EditPlantLambda;
import com.ohboywerecamping.webapp.plant.ReadPlantLambda;
import com.ohboywerecamping.webapp.plant.ReadPlantListLambda;
import com.ohboywerecamping.webapp.util.JsonUtils;

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
