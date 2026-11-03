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
        Frequency min,
        Frequency max,
        Optional<Frequency> defaultFrequency,
        Optional<String> pathTypeOverride,
        Holder<Modulation> modulation
) {
    public static final Codec<RadioBand> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Frequency.CODEC.fieldOf("min").forGetter(RadioBand::min),
            Frequency.CODEC.fieldOf("max").forGetter(RadioBand::max),
            Frequency.CODEC.optionalFieldOf("defaultFrequency").forGetter(RadioBand::defaultFrequency),
            Codec.STRING.optionalFieldOf("path_type").forGetter(RadioBand::pathTypeOverride),
            RegistryFixedCodec.create(AirwaveRegistries.MODULATION)
                    .optionalFieldOf("modulation", AirwaveModulations.FM)
                    .forGetter(RadioBand::modulation)
    ).apply(inst, RadioBand::new));

    public RadioBand {
        if (!min.isFinite() || !max.isFinite() || min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Invalid radio band range: " + min + " – " + max);
        }

        defaultFrequency.ifPresent(frequency -> {
            if (!frequency.isFinite() || frequency.compareTo(min) < 0 || frequency.compareTo(max) > 0) {
                throw new IllegalArgumentException("Default frequency must be finite and within the band range");
            }
        });
    }

    public RadioBand(Frequency min, Frequency max) {
        this(min, max, Optional.empty(), Optional.empty(), AirwaveModulations.FM);
    }

    public RadioBand(Frequency min, Frequency max, Frequency defaultFrequency) {
        this(min, max, Optional.of(defaultFrequency), Optional.empty(), AirwaveModulations.FM);
    }

    public RadioBand withPathType(String pathType) {
        return new RadioBand(min, max, defaultFrequency, Optional.of(pathType), modulation);
    }

    public RadioBand withModulation(Holder<Modulation> modulation) {
        return new RadioBand(min, max, defaultFrequency, pathTypeOverride, modulation);
    }

    public String pathType(ResourceLocation id) {
        return pathTypeOverride.orElseGet(() -> id.getPath().toUpperCase(Locale.ROOT));
    }

    public Frequency clamp(Frequency frequency) {
        Frequency f = frequency.isFinite() ? frequency : defaultFrequency.orElse(min);

        if (f.compareTo(min) < 0) {
            f = min;
        } else if (f.compareTo(max) > 0) {
            f = max;
        }

        return f.rounded();
    }

    public String display(Frequency frequency, ResourceLocation id) {
        return String.format(Locale.ROOT, "%.2f %s %s",
                frequency.frequency(), frequency.unit().getSymbol(), pathType(id));
    }
}