package gardenmanager.webapp.species;

import java.util.Objects;

public class Id {
    private final String gardenerId;
    private final String speciesId;

    public Id(final String gardenerId, final String speciesId) {
        this.gardenerId = gardenerId;
        this.speciesId = speciesId;
    }

    public String getGardenerId() {
        return gardenerId;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Id id = (Id) o;
        return gardenerId.equals(id.gardenerId) && speciesId.equals(id.speciesId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gardenerId, speciesId);
    }

    @Override
    public String toString() {
        return gardenerId + ':' + speciesId;
    }

    public static Id fromString(final String s) {
        String[] parts = s.split(":", 2);
        return new Id(parts[0], parts[1]);
    }
}
