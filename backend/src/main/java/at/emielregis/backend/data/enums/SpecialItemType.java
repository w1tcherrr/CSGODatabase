package at.emielregis.backend.data.enums;

public enum SpecialItemType {
    NONE,
    STAT_TRAK,
    SOUVENIR;

    public static SpecialItemType fromBooleans(boolean statTrak, boolean souvenir) {
        if (statTrak && souvenir) {
            throw new IllegalStateException("Can't parse Special Item Type!");
        }
        if (statTrak) {
            return STAT_TRAK;
        } else if (souvenir) {
            return SOUVENIR;
        } else {
            return NONE;
        }
    }
}
