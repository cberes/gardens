package gardenmanager.domain;

import java.time.ZonedDateTime;

public class Plant {
    private String id;
    private String gardenerId;
    private String speciesId;
    private String garden;
    private ZonedDateTime planted;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getGardenerId() {
        return gardenerId;
    }

    public void setGardenerId(final String gardenerId) {
        this.gardenerId = gardenerId;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(final String speciesId) {
        this.speciesId = speciesId;
    }

    public String getGarden() {
        return garden;
    }

    public void setGarden(final String garden) {
        this.garden = garden;
    }

    public ZonedDateTime getPlanted() {
        return planted;
    }

    public void setPlanted(final ZonedDateTime planted) {
        this.planted = planted;
    }
}
