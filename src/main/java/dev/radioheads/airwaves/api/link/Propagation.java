package dev.radioheads.airwaves.api.link;

import java.util.Optional;

public final class Propagation {
    public static final double SURFACE_BLOCKS_PER_KM = 59.0;
    // Metres per block along the ground
    public static final double METRES_PER_BLOCK = 1000.0 / SURFACE_BLOCKS_PER_KM;
    // Blocks per km on the sky path
    private static final double SKY_BLOCKS_PER_KM = 5.0;
    private static final double HORIZON_KM_PER_ROOT_BLOCK = 4.12;
    private static final double IONOSPHERE_KM = 300.0;
    private static final double MAX_HOP_KM = 3500.0;
    private static final double MAX_SECANT = 3.5;
    private static final double MUF_EDGE = 0.15;
    private static final double MUF_EDGE_DB = 30.0;
    private static final double GROUND_BOUNCE_DB = 3.0;
    // Ground wave attenuation ratio of land to sea
    private static final double SEA_ADVANTAGE = 20.0;
    // Cross-polarization loss in dB
    public static final double CROSS_POLARIZATION_DB = 10.0;
    // Cover loss per block at 1 MHz and its cap in dB
    private static final double COVER_DB_PER_BLOCK_AT_1_MHZ = 1.7;
    private static final double MAX_COVER_DB_PER_BLOCK = 4.0;
    // Receiver skirt rejection in dB
    private static final double SKIRT_REJECTION_DB = 60.0;

    public record SkyPath(double lossDb, double elevationDeg) {}

    private Propagation() {}

    // Surface path loss
    public static double surfaceLossDb(double mhz, double blocks, double heightA, double heightB, double obstructionDb,
                                       double seaFraction, double polarizationDb, SkyState sky) {
        double km = blocks / sky.reachFactor(mhz) / SURFACE_BLOCKS_PER_KM;
        double lineOfSight = lineOfSightDb(mhz, km, heightA, heightB, obstructionDb) + polarizationDb;
        return Math.min(lineOfSight, groundWaveDb(mhz, km, seaFraction));
    }

    // F-layer sky wave hop path
    public static Optional<SkyPath> skyPath(double mhz, double blocks, SkyState sky) {
        if (!sky.ionosphere()) {
            return Optional.empty();
        }

        double km = blocks / sky.reachFactor(mhz) / SKY_BLOCKS_PER_KM;
        int hops = Math.max(1, (int) Math.ceil(km / MAX_HOP_KM));
        double hop = km / hops;
        double secant = Math.min(MAX_SECANT, Math.sqrt(1.0 + Math.pow(hop / (2.0 * IONOSPHERE_KM), 2.0)));
        double muf = sky.criticalMhz() * secant;
        if (mhz > muf * (1.0 + MUF_EDGE)) {
            return Optional.empty();
        }

        double edge = Math.max(0.0, (mhz / muf - 1.0) / MUF_EDGE) * MUF_EDGE_DB;
        double path = hops * 2.0 * Math.hypot(hop / 2.0, IONOSPHERE_KM);
        double loss = freeSpaceDb(mhz, path) + hops * sky.absorptionDb(mhz, secant) + (hops - 1) * GROUND_BOUNCE_DB + edge;
        return Optional.of(new SkyPath(loss, Math.toDegrees(Math.atan2(IONOSPHERE_KM, hop / 2.0))));
    }

    // Noise floor from kTB and receiver and atmospheric noise
    public static double noiseDbm(double mhz, double bandwidthHertz, double noiseFigureDb, double efficiencyDb, SkyState sky) {
        double external = fromDb(sky.atmosphericNoiseDb(mhz));
        return -174.0 + 10.0 * Math.log10(bandwidthHertz) + 10.0 * Math.log10(fromDb(noiseFigureDb) + external * fromDb(efficiencyDb));
    }

    // Cover loss
    public static double coverDb(double mhz, int blocks) {
        return blocks * Math.min(COVER_DB_PER_BLOCK_AT_1_MHZ * Math.sqrt(mhz), MAX_COVER_DB_PER_BLOCK);
    }

    // Adjacent channel rejection
    public static double channelRejectionDb(double offsetHertz, double bandwidthHertz) {
        double overlap = (bandwidthHertz - Math.abs(offsetHertz)) / bandwidthHertz;
        return overlap <= 0.0 ? SKIRT_REJECTION_DB : Math.clamp(-10.0 * Math.log10(overlap), 0.0, SKIRT_REJECTION_DB);
    }

    // single knife-edge diffraction
    public static double knifeEdgeDb(double mhz, double obstructionBlocks, double blocksA, double blocksB) {
        double wavelength = 300.0 / mhz;
        double metresA = blocksA * METRES_PER_BLOCK;
        double metresB = blocksB * METRES_PER_BLOCK;
        double v = obstructionBlocks * Math.sqrt(2.0 * (metresA + metresB) / (wavelength * metresA * metresB));
        if (v < -0.78) return 0.0;
        double w = v - 0.1;
        return 6.9 + 20.0 * Math.log10(Math.sqrt(w * w + 1.0) + w);
    }

    // Line of sight loss from free space and two-ray plane earth
    private static double lineOfSightDb(double mhz, double km, double heightA, double heightB, double obstructionDb) {
        double planeEarth = 40.0 * Math.log10(Math.max(km * 1000.0, 1.0)) - 20.0 * Math.log10(heightA) - 20.0 * Math.log10(heightB);
        double loss = Math.max(freeSpaceDb(mhz, km), planeEarth) + obstructionDb;
        double horizon = HORIZON_KM_PER_ROOT_BLOCK * (Math.sqrt(heightA) + Math.sqrt(heightB));
        return km > horizon ? loss + 60.0 * Math.log10(km / horizon) : loss;
    }

    // Ground wave loss
    private static double groundWaveDb(double mhz, double km, double seaFraction) {
        double landNepersPerKm = Math.pow(mhz, 1.2) / 18.0;
        return freeSpaceDb(mhz, km) + 8.686 * km * landNepersPerKm * (1.0 - seaFraction + seaFraction / SEA_ADVANTAGE);
    }

    // Friis free-space path loss
    private static double freeSpaceDb(double mhz, double km) {
        return 32.45 + 20.0 * Math.log10(mhz) + 20.0 * Math.log10(Math.max(km, 0.001));
    }

    private static double fromDb(double db) {
        return Math.pow(10.0, db / 10.0);
    }
}
