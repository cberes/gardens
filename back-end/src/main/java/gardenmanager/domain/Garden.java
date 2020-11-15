package gardenmanager.domain;

import java.util.Objects;

public class Garden {
    private String gardenerId;
    private String name;

    public String getGardenerId() {
        return gardenerId;
    }

    public void setGardenerId(final String gardenerId) {
        this.gardenerId = gardenerId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Garden garden = (Garden) o;
        return Objects.equals(gardenerId, garden.gardenerId)
                && Objects.equals(name, garden.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gardenerId, name);
    }
}
