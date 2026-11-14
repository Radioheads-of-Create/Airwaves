package dev.radioheads.airwaves.api.cable;

// Cable run line type
public enum LineType {
    NONE,
    RADIO,
    TELEPHONE;

    public static LineType of(int ordinal) {
        LineType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
    }

    public boolean clashes(LineType other) {
        return this != NONE && other != NONE && this != other;
    }
}
