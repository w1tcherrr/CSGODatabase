package at.emielregis.backend.data.enums;

public enum Rarity {
    STOCK,                  // STOCK ITEMS
    CONSUMER_GRADE,         // WHITE
    INDUSTRIAL_GRADE,       // LIGHT BLUE
    MIL_SPEC,               // BLUE
    RESTRICTED,             // PURPLE
    CLASSIFIED,             // PINK
    COVERT,                 // RED

    CONTRABAND,             // DISCONTINUED, HOWL STICKER

    EXTRAORDINARY,          // GOLD (STICKERS, GLOVES, KNIFES, ETC.)

    BASE_GRADE,             // CONTAINERS LIKE STICKER CAPSULES

    HIGH_GRADE,             // NORMAL STICKERS
    REMARKABLE,             // HOLO STICKERS
    EXOTIC,                 // FOIL STICKERS

    DISTINGUISHED,          // AGENTS
    EXCEPTIONAL,            // AGENTS
    SUPERIOR,               // AGENTS
    MASTER;                 // AGENTS

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
