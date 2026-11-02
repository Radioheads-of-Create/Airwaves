package dev.radioheads.airwaves.api.antenna;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.link.LinkEnd;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public record AntennaProfile(Shape shape, double lengthBlocks, double resistivity, Vec3 radiator) {
    public static final Codec<AntennaProfile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Shape.CODEC.fieldOf("shape").forGetter(AntennaProfile::shape),
            Codec.DOUBLE.fieldOf("length_blocks").forGetter(AntennaProfile::lengthBlocks),
            Codec.DOUBLE.optionalFieldOf("resistivity", 1.0).forGetter(AntennaProfile::resistivity),
            Vec3.CODEC.fieldOf("radiator").forGetter(AntennaProfile::radiator)
    ).apply(inst, AntennaProfile::new));

    private static final double LIGHT = 300.0;
    // Rod radius and bare connector pin length in blocks
    private static final double ROD_RADIUS_BLOCKS = 0.125;
    private static final double BARE_STUB_BLOCKS = 0.03;
    // Whip ground loss in ohms
    private static final double GROUND_OHMS = 12.0;
    // Loading coil Q and tuner Q and radio impedance
    private static final double COIL_Q = 100.0;
    private static final double TUNER_Q = 100.0;
    private static final double RADIO_OHMS = 50.0;
    private static final double MIN_SIN_SQUARED = 0.01;
    private static final double COPPER_OHM_METRES = 1.68E-8;
    private static final double MU_0 = 4.0E-7 * Math.PI;
    // Real ground loss of a whip in dB
    private static final double REAL_GROUND_DB = 3.0;
    private static final double MIN_GAIN = 0.01;

    public enum Shape {
        NONE,
        WHIP;

        public static final Codec<Shape> CODEC = Codec.STRING.xmap(name -> "none".equals(name) ? NONE : WHIP, shape -> shape.name().toLowerCase(Locale.ROOT));
    }

    public static AntennaProfile bare(Vec3 position) {
        return new AntennaProfile(Shape.NONE, BARE_STUB_BLOCKS, 1.0, position);
    }

    public static AntennaProfile whip(double heightBlocks, Vec3 radiator) {
        return new AntennaProfile(Shape.WHIP, heightBlocks, 1.0, radiator);
    }

    public static AntennaProfile of(Set<BlockPos> rods, ToDoubleFunction<BlockPos> resistivity) {
        int minX = rods.stream().mapToInt(BlockPos::getX).min().orElseThrow();
        int maxX = rods.stream().mapToInt(BlockPos::getX).max().orElseThrow();
        int minY = rods.stream().mapToInt(BlockPos::getY).min().orElseThrow();
        int maxY = rods.stream().mapToInt(BlockPos::getY).max().orElseThrow();
        int minZ = rods.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        int maxZ = rods.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
        double metal = rods.stream().mapToDouble(resistivity).average().orElse(1.0);
        Vec3 radiator = new Vec3((minX + maxX + 1) / 2.0, maxY + 1.0, (minZ + maxZ + 1) / 2.0);
        return new AntennaProfile(Shape.WHIP, maxY - minY + 1, metal, radiator);
    }

    // Radiation efficiency
    public double efficiencyDb(double mhz) {
        double a = electricalLength(mhz);
        double sine = Math.sin(a);
        double sineSquared = Math.max(sine * sine, MIN_SIN_SQUARED);
        double radiation = 30.0 * Wire.power(a) / sineSquared;
        double z0 = 60.0 * (Math.log(Math.max(2.0 * this.lengthBlocks / ROD_RADIUS_BLOCKS, 4.0)) - 1.0);
        double reactance = z0 * Math.abs(Math.cos(a)) / Math.sqrt(sineSquared);
        double skin = Math.sqrt(Math.PI * mhz * 1.0E6 * MU_0 * COPPER_OHM_METRES * this.resistivity);
        double metal = skin * this.lengthBlocks / (2.0 * Math.PI * ROD_RADIUS_BLOCKS) / 2.0 / sineSquared;
        double resistance = radiation + GROUND_OHMS + metal + reactance / COIL_Q;
        double loadedQ = Math.sqrt(Math.max(resistance, RADIO_OHMS) / Math.min(resistance, RADIO_OHMS) - 1.0);
        return 10.0 * Math.log10(radiation / resistance) - 10.0 * Math.log10(1.0 + loadedQ / TUNER_Q);
    }

    // Whip directivity at an elevation
    public double directivityDb(double mhz, double elevationDeg) {
        double a = electricalLength(mhz);
        double field = Wire.field(a, Math.PI / 2.0 - Math.toRadians(elevationDeg));
        double gain = 4.0 * field * field / Wire.power(a) / Math.pow(10.0, REAL_GROUND_DB / 10.0);
        return 10.0 * Math.log10(Math.max(gain, MIN_GAIN));
    }

    private double electricalLength(double mhz) {
        return 2.0 * Math.PI * this.lengthBlocks / (LIGHT / mhz);
    }

    public LinkEnd linkEnd(double watts, double feedLossDb, double noiseFigureDb, double heightBlocks, double mhz) {
        double efficiency = efficiencyDb(mhz);
        return new LinkEnd(watts, heightBlocks, efficiency + directivityDb(mhz, 0.0), efficiency, noiseFigureDb, feedLossDb,
                elevation -> efficiency + directivityDb(mhz, elevation));
    }
}
