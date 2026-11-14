package dev.radioheads.airwaves.api.cable;

// Telephone line side at an insulator
public enum LineSide {
    NONE,
    TX,
    RX;

    public static LineSide of(int ordinal) {
        LineSide[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
    }
}
