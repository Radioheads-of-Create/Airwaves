package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record RadioBand(
        long minHertz,
        long maxHertz,
        long stepHertz,
        long defaultHertz,
        String label,
        Holder<Modulation> modulation
) {
    public static final Codec<RadioBand> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.LONG.fieldOf("min_hertz").forGetter(RadioBand::minHertz),
            Codec.LONG.fieldOf("max_hertz").forGetter(RadioBand::maxHertz),
            Codec.LONG.fieldOf("step_hertz").forGetter(RadioBand::stepHertz),
            Codec.LONG.fieldOf("default_hertz").forGetter(RadioBand::defaultHertz),
            Codec.STRING.fieldOf("label").forGetter(RadioBand::label),
            RegistryFixedCodec.create(AirwaveRegistries.MODULATION).fieldOf("modulation").forGetter(RadioBand::modulation)
    ).apply(inst, RadioBand::new));

    public RadioBand {
        if (minHertz <= 0 || minHertz >= maxHertz) {
            throw new IllegalArgumentException("Invalid radio band range: " + minHertz + "–" + maxHertz);
        }

        if (stepHertz <= 0 || (maxHertz - minHertz) % stepHertz != 0) {
            throw new IllegalArgumentException("Step must divide the band range: " + stepHertz);
        }

        if (defaultHertz < minHertz || defaultHertz > maxHertz || (defaultHertz - minHertz) % stepHertz != 0) {
            throw new IllegalArgumentException("Default frequency must sit on a step within the band range");
        }
    }

    public boolean contains(long hertz) {
        return hertz >= minHertz && hertz <= maxHertz;
    }

    public long clamp(long hertz) {
        long clamped = Math.clamp(hertz, minHertz, maxHertz);
        return minHertz + Math.round((clamped - minHertz) / (double) stepHertz) * stepHertz;
    }

    public String display(long hertz) {
        boolean kilohertz = minHertz < 1_000_000L;
        int scale = kilohertz ? 3 : 6;
        int decimals = Math.max(0, BigDecimal.valueOf(stepHertz, scale).stripTrailingZeros().scale());
        String value = BigDecimal.valueOf(hertz, scale).setScale(decimals, RoundingMode.HALF_UP).toPlainString();
        return value + (kilohertz ? " kHz " : " MHz ") + label;
    }
}
