package gardenmanager.webapp.species;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class PlantId {
    private final String speciesId;
    private final String plantId;

    public PlantId(final String speciesId, final String plantId) {
        this.speciesId = speciesId;
        this.plantId = plantId;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public String getPlantId() {
        return plantId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PlantId id = (PlantId) o;
        return speciesId.equals(id.speciesId)
                && plantId.equals(id.plantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(speciesId, plantId);
    }

    @Override
    public String toString() {
        // assume we don't know the characters that make up the species ID
        // so base64 encode the species ID in case it contains a color
        return encode(speciesId) + ":" + plantId;
    }

    private static String encode(final String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static PlantId fromString(final String s) {
        String[] parts = s.split(":", 2);
        return new PlantId(decode(parts[0]), parts[1]);
    }

    private static String decode(final String s) {
        return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
    }
}
