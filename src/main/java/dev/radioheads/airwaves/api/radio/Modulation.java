package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Modulation(int bandwidthHertz, int audioHertz, double thresholdDb, double fadeDb, double captureDb) {
    public static final Codec<Modulation> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("bandwidth_hertz").forGetter(Modulation::bandwidthHertz),
            Codec.INT.fieldOf("audio_hertz").forGetter(Modulation::audioHertz),
            Codec.DOUBLE.fieldOf("threshold_db").forGetter(Modulation::thresholdDb),
            Codec.DOUBLE.fieldOf("fade_db").forGetter(Modulation::fadeDb),
            Codec.DOUBLE.fieldOf("capture_db").forGetter(Modulation::captureDb)
    ).apply(inst, Modulation::new));

    public Modulation {
        if (bandwidthHertz <= 0)
            throw new IllegalArgumentException("bandwidthHertz must be > 0: " + bandwidthHertz);
        if (audioHertz <= 0)
            throw new IllegalArgumentException("audioHertz must be > 0: " + audioHertz);
        if (!Double.isFinite(thresholdDb))
            throw new IllegalArgumentException("thresholdDb must be finite: " + thresholdDb);
        if (!Double.isFinite(fadeDb) || fadeDb <= 0.0)
            throw new IllegalArgumentException("fadeDb must be finite and > 0: " + fadeDb);
        if (!Double.isFinite(captureDb) || captureDb <= 0.0)
            throw new IllegalArgumentException("captureDb must be finite and > 0: " + captureDb);
    }

    public float quality(double marginDb) {
        return (float) Math.clamp(marginDb / fadeDb, 0.0, 1.0);
    }
}
