package com.ohboywerecamping.domain;

import java.time.ZonedDateTime;

public class GardenPlant {
    private String id;
    private String gardenerId;
    private String plantId;
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

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(final String plantId) {
        this.plantId = plantId;
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
