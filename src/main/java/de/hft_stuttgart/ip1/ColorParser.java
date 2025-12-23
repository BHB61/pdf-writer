package de.hft_stuttgart.ip1;

import java.awt.*;
import java.lang.reflect.Field;

public class ColorParser {
    public static Color parseColor(String value) {
        String normalized = value.trim();
        if (normalized.startsWith("0x")) {
            return new Color(Integer.parseInt(normalized.substring(2), 16));
        }
        if (normalized.startsWith("#")) {
            return new Color(Integer.parseInt(normalized.substring(1), 16));
        }
        try {
            Field field = Color.class.getField(normalized);
            return (Color) field.get(null);
        } catch (ReflectiveOperationException ignored) {
            try {
                Field field = Color.class.getField(normalized.toLowerCase());
                return (Color) field.get(null);
            } catch (ReflectiveOperationException ignoredAgain) {
                return Color.BLACK;
            }
        }
    }
}
