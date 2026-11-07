package dev.radioheads.airwaves.api.link;

import dev.radioheads.airwaves.api.antenna.Polarization;

import java.util.function.DoubleUnaryOperator;

// One end of a link
public record LinkEnd(double watts, double heightBlocks, double gainDb, double efficiencyDb, double noiseFigureDb,
                      double feedLossDb, Polarization polarization, DoubleUnaryOperator skyGainDb) {
    public LinkEnd(double watts, double heightBlocks, double gainDb, double efficiencyDb, double noiseFigureDb) {
        this(watts, heightBlocks, gainDb, efficiencyDb, noiseFigureDb, 0.0, Polarization.VERTICAL, elevation -> gainDb);
    }

    public static LinkEnd handheld(double heightBlocks) {
        return new LinkEnd(5.0, heightBlocks, -3.0, -3.0, 8.0);
    }

    // Extra antenna loss
    public LinkEnd withLossDb(double db) {
        return new LinkEnd(this.watts, this.heightBlocks, this.gainDb - db, this.efficiencyDb - db, this.noiseFigureDb,
                this.feedLossDb, this.polarization, elevation -> this.skyGainDb.applyAsDouble(elevation) - db);
    }
}
