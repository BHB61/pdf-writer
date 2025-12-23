package de.hft_stuttgart.ip1;

public enum TextAlignment {
    LEFT,
    CENTER,
    RIGHT;

    public static TextAlignment from(String value) {
        return switch (value.toLowerCase()) {
            case "center" -> CENTER;
            case "right" -> RIGHT;
            default -> LEFT;
        };
    }
}
