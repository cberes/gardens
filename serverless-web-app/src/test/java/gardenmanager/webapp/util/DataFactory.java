package gardenmanager.webapp.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import gardenmanager.domain.LightPreference;
import gardenmanager.domain.MoisturePreference;
import gardenmanager.domain.Plant;
import gardenmanager.domain.Species;

public final class DataFactory {
    private DataFactory() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
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
