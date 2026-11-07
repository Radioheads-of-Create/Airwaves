package dev.radioheads.airwaves.api.link;

import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioStation;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record LinkBudget(double pathLossDb, double receivedDbm, double noiseDbm, double marginDb, float quality, boolean viaSky) {
    // Terrain around a link
    public record Surroundings(double obstructionDb, double seaFraction, double coverDb) {
        public static Surroundings open(double obstructionDb) {
            return new Surroundings(obstructionDb, 0.0, 0.0);
        }
    }

    public static LinkBudget between(RadioStation tx, RadioStation rx, RadioTerrain terrain, SkyState sky) {
        double mhz = tx.hertz() / 1.0E6;
        Vec3 from = tx.antenna();
        Vec3 to = rx.antenna();
        return evaluate(
                tx.linkEnd(mhz, tx.siteHeight(terrain), to),
                rx.linkEnd(mhz, rx.siteHeight(terrain), from),
                tx.hertz(),
                tx.modulation().value(),
                from.distanceTo(to),
                surroundings(terrain, mhz, from, to),
                sky
        );
    }

    public static Surroundings surroundings(RadioTerrain terrain, double mhz, Vec3 from, Vec3 to) {
        RadioTerrain.Path path = terrain.path(mhz, from, to);
        double cover = Propagation.coverDb(mhz, terrain.cover(from)) + Propagation.coverDb(mhz, terrain.cover(to));
        return new Surroundings(path.obstructionDb(), path.seaFraction(), cover);
    }

    public static LinkBudget evaluate(LinkEnd tx, LinkEnd rx, long hertz, Modulation modulation, double blocks, double obstructionDb, SkyState sky) {
        return evaluate(tx, rx, hertz, modulation, blocks, Surroundings.open(obstructionDb), sky);
    }

    // Link budget
    public static LinkBudget evaluate(LinkEnd tx, LinkEnd rx, long hertz, Modulation modulation, double blocks, Surroundings around, SkyState sky) {
        double mhz = hertz / 1.0E6;
        double crossed = tx.polarization() == rx.polarization() ? 0.0 : Propagation.CROSS_POLARIZATION_DB;
        double surfaceLoss = Propagation.surfaceLossDb(mhz, blocks, tx.heightBlocks(), rx.heightBlocks(),
                around.obstructionDb(), around.seaFraction(), crossed, sky);
        double surface = tx.gainDb() + rx.gainDb() - surfaceLoss;

        Optional<Propagation.SkyPath> skyPath = Propagation.skyPath(mhz, blocks, sky);
        double skyward = skyPath.map(path -> tx.skyGainDb().applyAsDouble(path.elevationDeg())
                + rx.skyGainDb().applyAsDouble(path.elevationDeg()) - path.lossDb()).orElse(Double.NEGATIVE_INFINITY);
        boolean viaSky = skyward > surface;

        double received = 30.0 + 10.0 * Math.log10(tx.watts()) - tx.feedLossDb() + Math.max(surface, skyward) - around.coverDb();
        double noise = Propagation.noiseDbm(mhz, modulation.bandwidthHertz(), rx.noiseFigureDb(), rx.efficiencyDb(), sky);
        double margin = received - noise - modulation.thresholdDb();
        return new LinkBudget(viaSky ? skyPath.get().lossDb() : surfaceLoss, received, noise, margin, modulation.quality(margin), viaSky);
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
