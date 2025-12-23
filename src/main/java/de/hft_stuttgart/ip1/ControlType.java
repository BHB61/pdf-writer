package de.hft_stuttgart.ip1;

public enum ControlType {
    TEXTBOX,
    DROPDOWN,
    RADIO,
    OPTION;

    public static ControlType from(String value, String optionData) {
        if ("dropdown".equalsIgnoreCase(value)) {
            return DROPDOWN;
        }
        if ("radio".equalsIgnoreCase(value)) {
            return RADIO;
        }
        if ("option".equalsIgnoreCase(value)) {
            return OPTION;
        }
        return TEXTBOX;
    }
}
