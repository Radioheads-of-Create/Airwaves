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
import java.util.Optional;

@NothingNullByDefault
public record RadioBand(
        double min,
        double max,
        HertzUnit unit,
        Holder<Modulation> modulation,
        Optional<Double> defaultFrequency
) {
    public static final Codec<RadioBand> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("min").forGetter(RadioBand::min),
            Codec.DOUBLE.fieldOf("max").forGetter(RadioBand::max),
            HertzUnit.CODEC.optionalFieldOf("unit", HertzUnit.MHz).forGetter(RadioBand::unit),
            RegistryFixedCodec.create(AirwaveRegistries.MODULATION)
                    .optionalFieldOf("modulation", AirwaveModulations.FM)
                    .forGetter(RadioBand::modulation),
            Codec.DOUBLE.optionalFieldOf("default_frequency").forGetter(RadioBand::defaultFrequency)
    ).apply(inst, RadioBand::new));

    public RadioBand(double min, double max, HertzUnit unit, Holder<Modulation> modulation, double defaultFrequency) {
        this(min, max, unit, modulation, Optional.of(defaultFrequency));
    }

    public RadioBand {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
            throw new IllegalArgumentException("Invalid radio band range: " + min + " – " + max + " " + unit.getSymbol());
        }
        defaultFrequency.ifPresent(d -> {
            if (!Double.isFinite(d) || d < min || d > max) {
                throw new IllegalArgumentException("Default frequency must be finite and within the band range");
            }
        });
    }

    public static RadioBand mhz(double min, double max, double defaultFrequency) {
        return new RadioBand(min, max, HertzUnit.MHz, AirwaveModulations.FM, Optional.of(defaultFrequency));
    }

    public static RadioBand mhz(double min, double max) {
        return new RadioBand(min, max, HertzUnit.MHz, AirwaveModulations.FM, Optional.empty());
    }

    public Frequency minFrequency() { return Frequency.of(min, unit); }
    public Frequency maxFrequency() { return Frequency.of(max, unit); }
    public Frequency defaultOrMin() { return Frequency.of(defaultFrequency.orElse(min), unit); }

    public RadioBand withModulation(Holder<Modulation> modulation) {
        return new RadioBand(min, max, unit, modulation, defaultFrequency);
    }

    public Frequency clamp(Frequency frequency) {
        double value = frequency.isFinite()
                ? frequency.convertTo(unit).frequency()
                : defaultFrequency.orElse(min);
        value = Math.clamp(value, min, max);
        return Frequency.of(Math.round(value * 100.0) / 100.0, unit);
    }

    public String display(Frequency frequency, ResourceLocation id) {
        Frequency f = frequency.convertTo(unit);
        return String.format(Locale.ROOT, "%.2f %s %s", f.frequency(), unit.getSymbol(), id.getPath().toUpperCase(Locale.ROOT));
    }
}