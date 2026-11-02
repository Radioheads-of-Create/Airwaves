package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public record Frequency(
        double frequency,
        HertzUnit unit
) implements Comparable<Frequency> {
    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("frequency").forGetter(Frequency::frequency),
            HertzUnit.CODEC.fieldOf("unit").forGetter(Frequency::unit)
    ).apply(inst, Frequency::new));

    public static final StreamCodec<FriendlyByteBuf, Frequency> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Frequency::frequency,
            HertzUnit.STREAM_CODEC, Frequency::unit,
            Frequency::new
    );

    public static Frequency of(double frequency, HertzUnit unit) {

        return new Frequency(frequency, unit);
    }

    public double toHz() {
        return unit.toHz(frequency);
    }

    public double toMHz() {
        return unit.toMHz(frequency);
    }

    @Contract("_ -> new")
    public Frequency convertTo(HertzUnit target) {
        return new Frequency(unit.convertTo(frequency, target), target);
    }

    public boolean isFinite() {
        return Double.isFinite(frequency);
    }

    public Frequency rounded() {
        return new Frequency(Math.round(frequency * 100.0D) / 100.0D, unit);
    }

    @Override
    public int compareTo(Frequency other) {
        return Double.compare(toHz(), other.toHz());
    }
}
