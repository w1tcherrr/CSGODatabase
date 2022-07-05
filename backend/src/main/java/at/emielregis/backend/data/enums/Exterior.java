package at.emielregis.backend.data.enums;

import java.util.ArrayList;
import java.util.List;

public enum Exterior {
    FACTORY_NEW("Factory New"),
    MINIMAL_WEAR("Minimal Wear"),
    FIELD_TESTED("Field Tested"),
    WELL_WORN("Well Worn"),
    BATTLE_SCARRED("Battle Scarred"),
    STOCK("Stock"),
    NOT_PAINTED("Not Painted");

    private final String name;

    Exterior(String name) {
        this.name = name;
    }

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

    public static List<Exterior> getBaseExteriors() {
        return new ArrayList<>(List.of(FACTORY_NEW, MINIMAL_WEAR, FIELD_TESTED, WELL_WORN, BATTLE_SCARRED));
    }

    public String getName() {
        return name;
    }
}
