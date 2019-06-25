package eu.researchalps.db.model;

/**
 * level of a research structure.
 */
public enum LevelEnum {
    TEAM(1, "Equipe Interne"),
    UNITE(2, "Unité"),
    FEDERATION(3, "Fédératif");

    /**
     * code of the level
     */
    private int value;
    /**
     * label of the level
     */
    private String label;

    LevelEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValidValue(int value) {
        return value > 0 && value < 4;
    }

}
