package at.emielregis.backend.data.enums;

public enum Exterior {
    FACTORY_NEW,
    MINIMAL_WEAR,
    FIELD_TESTED,
    WELL_WORN,
    BATTLE_SCARRED,
    STOCK,
    NOT_PAINTED;

    public static Exterior of(String value) {
        return switch (value) {
            case "Factory New" -> FACTORY_NEW;
            case "Minimal Wear" -> MINIMAL_WEAR;
            case "Field-Tested" -> FIELD_TESTED;
            case "Well-Worn" -> WELL_WORN;
            case "Battle-Scarred" -> BATTLE_SCARRED;
            case "Stock" -> STOCK;
            case "Not Painted" -> NOT_PAINTED;
            default -> throw new IllegalArgumentException("Exterior can't have value: " + value);
        };
    }
}
