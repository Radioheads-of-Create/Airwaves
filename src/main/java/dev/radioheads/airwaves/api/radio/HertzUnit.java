package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@NothingNullByDefault
public enum HertzUnit implements StringRepresentable {
    Hz("Hz", 1.0),
    kHz("kHz", 1_000.0),
    MHz("MHz", 1_000_000.0);

    public static final Codec<HertzUnit> CODEC = StringRepresentable.fromEnum(HertzUnit::values);

    public static final StreamCodec<FriendlyByteBuf, HertzUnit> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(HertzUnit.class);

    private final String symbol;
    private final double hzPerUnit;

    HertzUnit(String symbol, double hzPerUnit) {
        this.symbol = symbol;
        this.hzPerUnit = hzPerUnit;
    }

    public String getSymbol() {
        return symbol;
    }

    public double toHz(double value) {
        return value * hzPerUnit;
    }

    public double toMHz(double value) {
        return value * hzPerUnit / MHz.hzPerUnit;
    }

    public double convertTo(double value, HertzUnit target) {
        return value * hzPerUnit / target.hzPerUnit;
    }

    public static HertzUnit fromString(String s) {
        for (HertzUnit unit : values()) {
            if (unit.symbol.equalsIgnoreCase(s)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown hertz unit: " + s);
    }

    @Override
    public String getSerializedName() {
        return symbol;
    }
}