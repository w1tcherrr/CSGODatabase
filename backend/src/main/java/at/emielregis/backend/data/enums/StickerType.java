package at.emielregis.backend.data.enums;

public enum StickerType {
    FOIL,
    GLITTER,
    GOLD,
    HOLO,
    NORMAL;

    public static StickerType ofName(String stickerName) {
        stickerName = stickerName.replace(", Champion", "");
        if (stickerName.contains("(Glitter")) {
            return StickerType.GLITTER;
        }
        if (stickerName.contains("(Holo")) {
            return StickerType.HOLO;
        }
        if (stickerName.contains("(Foil")) {
            return StickerType.FOIL;
        }
        if (stickerName.contains("(Gold")) {
            return StickerType.GOLD;
        }
        return StickerType.NORMAL;
    }
}
