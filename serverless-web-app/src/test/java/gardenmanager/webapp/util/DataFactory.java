package gardenmanager.webapp.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import gardenmanager.domain.*;

public final class DataFactory {
    private DataFactory() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static Gardener gardener(final String id, final String email) {
        final Gardener gardener = new Gardener();
        gardener.setId(id);
        gardener.setEmail(email);
        gardener.setJoined(ZonedDateTime.now(ZoneOffset.UTC));
        return gardener;
    }

    public static Species species(final String gardenerId) {
        Species species = new Species();
        species.setGardenerId(gardenerId);
        species.setName("Species 1");
        species.setAlternateName("Alt species 1");
        species.setLight(LightPreference.FULL);
        species.setMoisture(MoisturePreference.MEDIUM);
        return species;
    }

    public static Plant plant(final String gardenerId, final String speciesId, final String garden) {
        Plant plant = new Plant();
        plant.setGardenerId(gardenerId);
        plant.setSpeciesId(speciesId);
        plant.setGarden(garden);
        plant.setPlanted(ZonedDateTime.now(ZoneOffset.UTC));
        return plant;
    }
}
