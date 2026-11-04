package dev.radioheads.airwaves.api.link;

import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record LinkBudget(double pathLossDb, double receivedDbm, double noiseDbm, double marginDb, boolean viaSky) {
    // Terrain around a link
    public record Surroundings(double obstructionDb, double seaFraction, double coverDb) {
        public static Surroundings open(double obstructionDb) {
            return new Surroundings(obstructionDb, 0.0, 0.0);
        }
    }

    public static Surroundings surroundings(RadioTerrain terrain, double mhz, Vec3 from, Vec3 to) {
        RadioTerrain.Path path = terrain.path(mhz, from, to);
        double cover = Propagation.coverDb(mhz, terrain.cover(from)) + Propagation.coverDb(mhz, terrain.cover(to));
        return new Surroundings(path.obstructionDb(), path.seaFraction(), cover);
    }

    public static LinkBudget evaluate(LinkEnd tx, LinkEnd rx, double mhz, double bandwidthHertz, double thresholdDb, double blocks, double obstructionDb, SkyState sky) {
        return evaluate(tx, rx, mhz, bandwidthHertz, thresholdDb, blocks, Surroundings.open(obstructionDb), sky);
    }

    // Link budget
    public static LinkBudget evaluate(LinkEnd tx, LinkEnd rx, double mhz, double bandwidthHertz, double thresholdDb, double blocks, Surroundings around, SkyState sky) {
        double surfaceLoss = Propagation.surfaceLossDb(mhz, blocks, tx.heightBlocks(), rx.heightBlocks(),
                around.obstructionDb(), around.seaFraction(), sky);
        double surface = tx.gainDb() + rx.gainDb() - surfaceLoss;

        Optional<Propagation.SkyPath> skyPath = Propagation.skyPath(mhz, blocks, sky);
        double skyward = skyPath.map(path -> tx.skyGainDb().applyAsDouble(path.elevationDeg())
                + rx.skyGainDb().applyAsDouble(path.elevationDeg()) - path.lossDb()).orElse(Double.NEGATIVE_INFINITY);
        boolean viaSky = skyward > surface;

        double received = 30.0 + 10.0 * Math.log10(tx.watts()) - tx.feedLossDb() + Math.max(surface, skyward) - around.coverDb();
        double noise = Propagation.noiseDbm(mhz, bandwidthHertz, rx.noiseFigureDb(), rx.efficiencyDb(), sky);
        return new LinkBudget(viaSky ? skyPath.get().lossDb() : surfaceLoss, received, noise, received - noise - thresholdDb, viaSky);
    }

    // Cascaded SNR of a repeater chain
    public static double chainMarginDb(double firstDb, double secondDb, double thresholdDb) {
        double noise = Math.pow(10.0, -(firstDb + thresholdDb) / 10.0) + Math.pow(10.0, -(secondDb + thresholdDb) / 10.0);
        return -10.0 * Math.log10(noise) - thresholdDb;
    }

    public boolean heard() {
        return marginDb >= 0.0;
    }
}
