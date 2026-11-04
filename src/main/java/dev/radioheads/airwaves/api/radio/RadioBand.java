package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import dev.radioheads.airwaves.registry.AirwaveModulations;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

@NothingNullByDefault
public record RadioBand(
        double min,
        double max,
        HertzUnit unit,
        Holder<Modulation> modulation
) {
    public static final Codec<RadioBand> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("min").forGetter(RadioBand::min),
            Codec.DOUBLE.fieldOf("max").forGetter(RadioBand::max),
            HertzUnit.CODEC.optionalFieldOf("unit", HertzUnit.MHz).forGetter(RadioBand::unit),
            RegistryFixedCodec.create(AirwaveRegistries.MODULATION)
                    .optionalFieldOf("modulation", AirwaveModulations.FM)
                    .forGetter(RadioBand::modulation)
    ).apply(inst, RadioBand::new));

    public RadioBand {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
            throw new IllegalArgumentException("Invalid radio band range: " + min + " – " + max + " " + unit.getSymbol());
        }
    }

    public Frequency minFrequency() { return Frequency.of(min, unit); }
    public Frequency maxFrequency() { return Frequency.of(max, unit); }

    public Frequency clamp(Frequency frequency) {
        double value = frequency.convertTo(unit).frequency();
        value = Math.clamp(value, min, max);
        return Frequency.of(Math.round(value * 100.0) / 100.0, unit);
    }

    public String display(Frequency frequency, ResourceLocation id) {
        Frequency f = frequency.convertTo(unit);
        return String.format(Locale.ROOT, "%.2f %s %s", f.frequency(), unit.getSymbol(), id.getPath().toUpperCase(Locale.ROOT));
    }
}