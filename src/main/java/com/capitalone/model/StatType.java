package com.capitalone.model;

/**
 * This class represents the supported statistic calculations.
 * To allow handling of unsupported StatType's there is an enum named UNSUPPORTED.
 * Instead of dealing with a null value, the user of fromString method can use this enum value to handle
 * the unsupported case appropriately.
 */
public enum StatType {
    AVERAGE("average"),
    MAX("max"),
    MIN("min"),
    UNSUPPORTED("unsupported");

    private final String name;

    StatType(String s) {
        name = s;
    }

    /**
     * Determines equality between this StatType and a string value.
     * The check is case insensitive.
     * @param otherName the string value to compare
     * @return true if equal, otherwise false
     */
    private boolean equalsName(final String otherName) {
        return otherName != null && name.equalsIgnoreCase(otherName);
    }

    public String toString() {
        return this.name;
    }

    /**
     * Takes a string value and attempts to find the corresponding StatType.
     * UNSUPPORTED is returned if none found.
     * @param statName the string name of the statistic type
     * @return the StatType enum
     */
    public static StatType fromString(final String statName) {
        for (final StatType statType : StatType.values())
            if (statType.equalsName(statName))
                return statType;
        return UNSUPPORTED;
    }
}
