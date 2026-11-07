package dev.radioheads.airwaves.network;

import java.util.SplittableRandom;
import java.util.UUID;

// Sky wave QSB and Rayleigh fading
final class Fading {
    private static final double SKY_SWING_DB = 4.0;
    private static final double SKY_SWING_SLOW_DB = 3.0;
    private static final double SKY_PERIOD_TICKS = 13.0 * 20.0;
    private static final double SKY_PERIOD_SLOW_TICKS = 37.0 * 20.0;
    private static final long FLUTTER_TICKS = 20L;
    private static final double DEEPEST_DB = -15.0;
    private static final double PEAK_DB = 5.0;

    private Fading() {}

    static double db(UUID source, UUID receiver, boolean viaSky, boolean moving, long gameTime) {
        long seed = source.getMostSignificantBits() ^ Long.rotateLeft(source.getLeastSignificantBits(), 17)
                ^ Long.rotateLeft(receiver.getMostSignificantBits(), 31) ^ receiver.getLeastSignificantBits();
        if (viaSky) {
            double phase = (seed & 0xFFFFL) / 65536.0 * 2.0 * Math.PI;
            double slowPhase = ((seed >>> 16) & 0xFFFFL) / 65536.0 * 2.0 * Math.PI;
            return SKY_SWING_DB * Math.sin(2.0 * Math.PI * gameTime / SKY_PERIOD_TICKS + phase)
                    + SKY_SWING_SLOW_DB * Math.sin(2.0 * Math.PI * gameTime / SKY_PERIOD_SLOW_TICKS + slowPhase);
        }
        if (moving) {
            // Rayleigh fading
            double u = new SplittableRandom(seed ^ (gameTime / FLUTTER_TICKS)).nextDouble();
            return Math.clamp(10.0 * Math.log10(-Math.log(Math.max(u, 1.0E-9))), DEEPEST_DB, PEAK_DB);
        }
        return 0.0;
    }
}
