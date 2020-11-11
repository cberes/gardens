package gardenmanager.webapp.util;

public class Tables {
    private static final Tables instance = new Tables();

    private final String gardener = "GARDENER_" + AwsUtils.environmentName();
    private final String plant = "PLANT_" + AwsUtils.environmentName();
    private final String species = "SPECIES_" + AwsUtils.environmentName();

    public static String gardener() {
        return instance.gardener;
    }

    public static String plant() {
        return instance.plant;
    }

    public static String species() {
        return instance.species;
    }
}
