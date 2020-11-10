package gardenmanager.webapp.plant;

import java.util.Objects;

class Id {
    private final String gardenerId;
    private final String speciesId;
    private final String garden;

    Id(final String gardenerId, final String speciesId, final String garden) {
        this.gardenerId = gardenerId;
        this.speciesId = speciesId;
        this.garden = garden;
    }

    String getGardenerId() {
        return gardenerId;
    }

    String getSpeciesId() {
        return speciesId;
    }

    String getGarden() {
        return garden;
    }

    gardenmanager.webapp.species.Id toParentId() {
        return new gardenmanager.webapp.species.Id(gardenerId, speciesId);
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
        return gardenerId.equals(id.gardenerId) &&
                speciesId.equals(id.speciesId) &&
                garden.equals(id.garden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gardenerId, speciesId, garden);
    }

    @Override
    public String toString() {
        return toParentId() + ":" + garden;
    }

    static Id fromString(final String s) {
        final gardenmanager.webapp.species.Id parentId = gardenmanager.webapp.species.Id.fromString(s);
        String[] parts = parentId.getSpeciesId().split(":", 2);
        return new Id(parentId.getGardenerId(), parts[0], parts[1]);
    }
}
