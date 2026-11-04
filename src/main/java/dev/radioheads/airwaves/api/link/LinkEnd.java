package dev.radioheads.airwaves.api.link;

import java.util.function.DoubleUnaryOperator;

// One end of a link
public record LinkEnd(double watts, double heightBlocks, double gainDb, double efficiencyDb, double noiseFigureDb,
                      double feedLossDb, DoubleUnaryOperator skyGainDb) {
    public LinkEnd(double watts, double heightBlocks, double gainDb, double efficiencyDb, double noiseFigureDb) {
        this(watts, heightBlocks, gainDb, efficiencyDb, noiseFigureDb, 0.0, elevation -> gainDb);
    }

    public static LinkEnd handheld(double heightBlocks) {
        return new LinkEnd(5.0, heightBlocks, -3.0, -3.0, 8.0);
    }

    // Extra antenna loss
    public LinkEnd withLossDb(double db) {
        return new LinkEnd(this.watts, this.heightBlocks, this.gainDb - db, this.efficiencyDb - db, this.noiseFigureDb,
                this.feedLossDb, elevation -> this.skyGainDb.applyAsDouble(elevation) - db);
    }
}
