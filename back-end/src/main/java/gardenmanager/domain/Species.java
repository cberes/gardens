package gardenmanager.domain;

public class Species {
    private String id;
    private String gardenerId;
    private String name;
    private String alternateName;
    private MoisturePreference moisture;
    private LightPreference light;

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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(final String alternateName) {
        this.alternateName = alternateName;
    }

    public MoisturePreference getMoisture() {
        return moisture;
    }

    public void setMoisture(final MoisturePreference moisture) {
        this.moisture = moisture;
    }

    public LightPreference getLight() {
        return light;
    }

    public void setLight(final LightPreference light) {
        this.light = light;
    }
}
