package gardenmanager.webapp.species;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class SpeciesId {
    private final String gardenerId;
    private final String speciesId;

    public SpeciesId(final String gardenerId, final String speciesId) {
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
        final SpeciesId id = (SpeciesId) o;
        return gardenerId.equals(id.gardenerId) && speciesId.equals(id.speciesId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gardenerId, speciesId);
    }

    @Override
    public String toString() {
        return encode(gardenerId) + ':' + speciesId;
    }

    private static String encode(final String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static SpeciesId fromString(final String s) {
        String[] parts = s.split(":", 2);
        return new SpeciesId(decode(parts[0]), parts[1]);
    }

    private static String decode(final String s) {
        return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
    }
}
