package dev.radioheads.airwaves.api.cable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Feed line between a station and its antenna
public record FeedLine(double conductorFactor, double blocks, double antennaSideFactor, double antennaSideBlocks, double amplifierDb) {
    public static final FeedLine NONE = new FeedLine(0.0, 0.0, 0.0, 0.0, 0.0);
    public static final Codec<FeedLine> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("conductor_factor").forGetter(FeedLine::conductorFactor),
            Codec.DOUBLE.fieldOf("blocks").forGetter(FeedLine::blocks),
            Codec.DOUBLE.fieldOf("antenna_side_factor").forGetter(FeedLine::antennaSideFactor),
            Codec.DOUBLE.fieldOf("antenna_side_blocks").forGetter(FeedLine::antennaSideBlocks),
            Codec.DOUBLE.fieldOf("amplifier_db").forGetter(FeedLine::amplifierDb)
    ).apply(inst, FeedLine::new));

    // Dielectric loss per block per MHz
    public static final double DIELECTRIC_DB_PER_BLOCK_MHZ = 5.4E-5;
    private static final double PREAMP_NOISE_FIGURE_DB = 2.0;

    public double lossDb(double mhz) {
        return loss(this.conductorFactor, this.blocks, mhz);
    }

    public double antennaSideLossDb(double mhz) {
        return loss(this.antennaSideFactor, this.antennaSideBlocks, mhz);
    }

    private static double loss(double factor, double blocks, double mhz) {
        return factor * Math.sqrt(mhz) + blocks * DIELECTRIC_DB_PER_BLOCK_MHZ * mhz;
    }

    // Friis noise cascade
    public double noiseFigureDb(double mhz, double receiverNoiseFigureDb) {
        double receiver = fromDb(receiverNoiseFigureDb);
        double total = fromDb(lossDb(mhz));
        if (this.amplifierDb <= 0.0) {
            return toDb(total * receiver);
        }

        double above = fromDb(antennaSideLossDb(mhz));
        double below = total / above;
        return toDb(above * (fromDb(PREAMP_NOISE_FIGURE_DB) + (below * receiver - 1.0) / fromDb(this.amplifierDb)));
    }

    private static double fromDb(double db) {
        return Math.pow(10.0, db / 10.0);
    }

    private static double toDb(double ratio) {
        return 10.0 * Math.log10(ratio);
    }
}
