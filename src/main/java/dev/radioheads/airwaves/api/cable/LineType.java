package dev.radioheads.airwaves.api.cable;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

// Cable run line type
public enum LineType implements StringRepresentable {
    NONE,
    RADIO,
    TELEPHONE;

    public static final Codec<LineType> CODEC = StringRepresentable.fromEnum(LineType::values);

    public static LineType of(int ordinal) {
        LineType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
    }

    public boolean clashes(LineType other) {
        return this != NONE && other != NONE && this != other;
    }

    @Override
    public String getSerializedName() {
        return toString().toLowerCase(Locale.ROOT);
    }
}
