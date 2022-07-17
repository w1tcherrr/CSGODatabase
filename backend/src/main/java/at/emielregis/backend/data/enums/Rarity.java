package at.emielregis.backend.data.enums;

import org.apache.poi.ss.usermodel.IndexedColors;

public enum Rarity {
    STOCK(IndexedColors.GREY_40_PERCENT),           // STOCK ITEMS
    CONSUMER_GRADE(IndexedColors.WHITE),            // WHITE
    INDUSTRIAL_GRADE(IndexedColors.AQUA),           // LIGHT BLUE
    MIL_SPEC(IndexedColors.LIGHT_BLUE),             // BLUE
    RESTRICTED(IndexedColors.VIOLET),               // PURPLE
    CLASSIFIED(IndexedColors.PINK),                 // PINK
    COVERT(IndexedColors.RED),                      // RED

    CONTRABAND(IndexedColors.LIGHT_ORANGE),         // DISCONTINUED, HOWL STICKER

    EXTRAORDINARY(IndexedColors.GOLD),              // GOLD (STICKERS, GLOVES, KNIFES, ETC.)

    BASE_GRADE(null),                    // CONTAINERS LIKE STICKER CAPSULES

    HIGH_GRADE(IndexedColors.GREY_40_PERCENT),      // NORMAL STICKERS
    REMARKABLE(IndexedColors.VIOLET),               // HOLO STICKERS
    EXOTIC(IndexedColors.PINK),                     // FOIL STICKERS

    DISTINGUISHED(IndexedColors.LIGHT_BLUE),        // AGENTS
    EXCEPTIONAL(IndexedColors.VIOLET),              // AGENTS
    SUPERIOR(IndexedColors.PINK),                   // AGENTS
    MASTER(IndexedColors.RED);                      // AGENTS

    private final IndexedColors indexedColor;

    Rarity(IndexedColors indexedColor) {
        this.indexedColor = indexedColor;
    }

    public IndexedColors getColor() {
        return indexedColor;
    }

    public static Rarity of(String value) {
        return switch (value) {
            case "Mil-Spec Grade" -> MIL_SPEC;
            case "Base Grade" -> BASE_GRADE;
            case "Industrial Grade" -> INDUSTRIAL_GRADE;
            case "Extraordinary" -> EXTRAORDINARY;
            case "Restricted" -> RESTRICTED;
            case "Consumer Grade" -> CONSUMER_GRADE;
            case "Contraband" -> CONTRABAND;
            case "Classified" -> CLASSIFIED;
            case "Exotic" -> EXOTIC;
            case "High Grade" -> HIGH_GRADE;
            case "Remarkable" -> REMARKABLE;
            case "Covert" -> COVERT;
            case "Stock" -> STOCK;
            case "Exceptional" -> EXCEPTIONAL;
            case "Distinguished" -> DISTINGUISHED;
            case "Superior" -> SUPERIOR;
            case "Master" -> MASTER;
            default -> throw new IllegalArgumentException("Rarity can't have value: " + value);
        };
    }
}
