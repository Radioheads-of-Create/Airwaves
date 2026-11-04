package dev.radioheads.airwaves.api.link;

import net.minecraft.server.level.ServerLevel;

import java.util.SplittableRandom;

public record SkyState(
        double daylight,
        double solarActivity,
        boolean flare,
        boolean storm,
        boolean tropo,
        boolean ionosphere
) {
    private static final SkyState NO_SKY = new SkyState(0.0, 0.0, false, false, false, false);
    private static final double HF_CEILING_MHZ = 30.0;
    private static final double TWILIGHT = 0.3;
    private static final double SOLAR_CYCLE_DAYS = 60.0;
    private static final double TICKS_PER_DAY = 24000.0;
    private static final long FLARE_TICKS = 1200L;
    // ITU-R P.372 atmospheric noise at 1 MHz and slope per decade
    private static final double NIGHT_STATIC_DB = 80.0;
    private static final double NIGHT_STATIC_SLOPE = 35.0;
    private static final double DAY_STATIC_DB = 50.0;
    private static final double DAY_STATIC_SLOPE = 28.0;
    private static final double STATIC_KNEE_MHZ = 10.0;
    private static final double STATIC_ROLL_OFF = 36.0;
    // Thunderstorm noise at 1 MHz
    private static final double THUNDER_STATIC_DB = 75.0;

    public static SkyState of(ServerLevel level) {
        if (!level.dimensionType().hasSkyLight()) return NO_SKY;
        return at(Math.cos(level.getSunAngle(1.0F)), level.isRaining(), level.isThundering(), level.getGameTime(), level.getSeed());
    }

    static SkyState at(double sun, boolean raining, boolean thundering, long gameTime, long seed) {
        double solar = solarActivity(gameTime, seed);
        return new SkyState(
                Math.pow(Math.max(sun, 0.0), 0.75),
                solar,
                flare(gameTime, seed, solar),
                thundering,
                !raining && Math.abs(sun) < TWILIGHT,
                true
        );
    }

    // Solar cycle
    static double solarActivity(long gameTime, long seed) {
        double day = gameTime / TICKS_PER_DAY + Math.floorMod(seed, (long) SOLAR_CYCLE_DAYS);
        return 0.5 - 0.5 * Math.cos(2.0 * Math.PI * day / SOLAR_CYCLE_DAYS);
    }

    // Solar flare
    static boolean flare(long gameTime, long seed, double solar) {
        return new SplittableRandom(seed ^ gameTime / FLARE_TICKS).nextDouble() < 0.01 + 0.04 * solar * solar;
    }

    // Tropospheric ducting
    public double reachFactor(double mhz) {
        return mhz >= HF_CEILING_MHZ && tropo ? 1.2 : 1.0;
    }

    // ITU-R P.372 atmospheric noise
    public double atmosphericNoiseDb(double mhz) {
        if (!ionosphere) return Double.NEGATIVE_INFINITY;
        double level = NIGHT_STATIC_DB + (DAY_STATIC_DB - NIGHT_STATIC_DB) * daylight;
        double slope = NIGHT_STATIC_SLOPE + (DAY_STATIC_SLOPE - NIGHT_STATIC_SLOPE) * daylight;
        double background = staticDb(level, slope, mhz);
        if (!storm) return background;
        return 10.0 * Math.log10(Math.pow(10.0, background / 10.0) + Math.pow(10.0, staticDb(THUNDER_STATIC_DB, DAY_STATIC_SLOPE, mhz) / 10.0));
    }

    private static double staticDb(double atOneMhz, double slope, double mhz) {
        double above = Math.max(0.0, Math.log10(mhz / STATIC_KNEE_MHZ));
        return atOneMhz - slope * Math.log10(mhz) - STATIC_ROLL_OFF * above * above;
    }

    // F-layer critical frequency
    public double criticalMhz() {
        double night = 2.5 + 3.0 * solarActivity;
        double noon = 5.0 + 7.0 * solarActivity;
        return night + (noon - night) * daylight;
    }

    // CCIR D-layer absorption
    public double absorptionDb(double mhz, double secant) {
        double sunlight = daylight * (1.0 + 0.8 * solarActivity) * (flare ? 10.0 : 1.0);
        return 677.2 * secant * Math.max(sunlight, 0.03) / (Math.pow(mhz + 1.4, 1.98) + 10.2);
    }
}
