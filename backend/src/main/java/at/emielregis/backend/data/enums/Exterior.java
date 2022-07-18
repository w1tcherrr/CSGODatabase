package at.emielregis.backend.data.enums;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<Exterior> extendBaseExteriors(List<Exterior> exteriors) {
        if (exteriors.contains(FACTORY_NEW) || exteriors.contains(MINIMAL_WEAR) || exteriors.contains(FIELD_TESTED) || exteriors.contains(WELL_WORN) || exteriors.contains(BATTLE_SCARRED)) {
            exteriors.addAll(getBaseExteriors());
        }
        exteriors = exteriors.stream().distinct().collect(Collectors.toList());
        exteriors.sort(Comparator.comparingInt(Enum::ordinal));
        return exteriors;
    }

    public String getName() {
        return name;
    }
}
