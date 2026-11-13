package dev.radioheads.airwaves.api.antenna;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Capacitance hat size in blocks
public record TopHat(double wireBlocks, double radiusBlocks) {
    public static final TopHat NONE = new TopHat(0.0, 0.0);
    public static final Codec<TopHat> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("wire_blocks").forGetter(TopHat::wireBlocks),
            Codec.DOUBLE.fieldOf("radius_blocks").forGetter(TopHat::radiusBlocks)
    ).apply(inst, TopHat::new));

    public boolean present() {
        return this.wireBlocks > 0.0 && this.radiusBlocks > 0.0;
    }
}
