package dev.radioheads.airwaves.api.antenna;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.antenna.pattern.CosinePattern;
import dev.radioheads.airwaves.api.link.LinkEnd;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.ToDoubleFunction;

public record AntennaProfile(Shape shape, double lengthBlocks, List<Integer> elementWidths, List<Integer> elementPositions,
                             double resistivity, Vec3 radiator, AntennaDirection beam) {
    public static final Codec<AntennaProfile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Shape.CODEC.fieldOf("shape").forGetter(AntennaProfile::shape),
            Codec.DOUBLE.fieldOf("length_blocks").forGetter(AntennaProfile::lengthBlocks),
            Codec.INT.listOf().optionalFieldOf("element_widths", List.of()).forGetter(AntennaProfile::elementWidths),
            Codec.INT.listOf().optionalFieldOf("element_positions", List.of()).forGetter(AntennaProfile::elementPositions),
            Codec.DOUBLE.optionalFieldOf("resistivity", 1.0).forGetter(AntennaProfile::resistivity),
            Vec3.CODEC.fieldOf("radiator").forGetter(AntennaProfile::radiator),
            AntennaDirection.CODEC.fieldOf("beam").forGetter(AntennaProfile::beam)
    ).apply(inst, AntennaProfile::new));

    private static final double LIGHT = 300.0;
    // Rod radius and bare connector pin length in blocks
    private static final double ROD_RADIUS_BLOCKS = 0.125;
    private static final double BARE_STUB_BLOCKS = 0.03;
    // Whip ground loss and dipole joint loss in ohms
    private static final double GROUND_OHMS = 12.0;
    private static final double JOINT_OHMS = 1.0;
    // Loading coil Q and tuner Q and radio impedance
    private static final double COIL_Q = 100.0;
    private static final double TUNER_Q = 100.0;
    private static final double RADIO_OHMS = 50.0;
    private static final double MIN_SIN_SQUARED = 0.01;
    private static final double COPPER_OHM_METRES = 1.68E-8;
    private static final double MU_0 = 4.0E-7 * Math.PI;
    // Real ground loss of a whip in dB
    private static final double REAL_GROUND_DB = 3.0;
    // Ground reflection coefficient for horizontal polarization
    private static final double GROUND_REFLECTION = 0.8;
    private static final double DIPOLE_GAIN_DB = 2.15;
    private static final double MIN_GAIN = 0.01;
    // Yagi element resonance range and maximum spacing in wavelengths
    private static final double MIN_ELEMENT_WAVES = 0.35;
    private static final double MAX_ELEMENT_WAVES = 0.65;
    private static final double MAX_SPACING_WAVES = 0.5;
    // NBS Yagi gain by boom length
    private static final double YAGI_DBD_AT_REFERENCE = 9.2;
    private static final double YAGI_DBD_PER_DECADE = 7.2;
    private static final double YAGI_REFERENCE_WAVES = 0.8;
    private static final double YAGI_BACK = 0.01;
    // Half-power level of a cos^1.5 beam and square degrees in a sphere
    private static final double HALF_POWER = Math.pow(0.5, 2.0 / 3.0);
    private static final double SPHERE_SQUARE_DEGREES = 41253.0;

    public enum Shape implements StringRepresentable {
        NONE,
        WHIP,
        DIPOLE,
        YAGI;

        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static AntennaProfile bare(Vec3 position) {
        return new AntennaProfile(Shape.NONE, BARE_STUB_BLOCKS, List.of(), List.of(), 1.0, position, AntennaDirection.DEFAULT);
    }

    public static AntennaProfile whip(double heightBlocks, Vec3 radiator) {
        return new AntennaProfile(Shape.WHIP, heightBlocks, List.of(), List.of(), 1.0, radiator, AntennaDirection.DEFAULT);
    }

    public static AntennaProfile dipole(double lengthBlocks, Vec3 radiator, AntennaDirection broadside) {
        return new AntennaProfile(Shape.DIPOLE, lengthBlocks, List.of(), List.of(), 1.0, radiator, broadside);
    }

    // Tooltip profile
    public static AntennaProfile display(Shape shape, double lengthBlocks, int elements, Vec3 position) {
        return new AntennaProfile(shape, lengthBlocks, Collections.nCopies(elements, 0), Collections.nCopies(elements, 0), 1.0, position, AntennaDirection.DEFAULT);
    }

    public static AntennaProfile of(Set<BlockPos> rods, ToDoubleFunction<BlockPos> resistivity) {
        int minX = rods.stream().mapToInt(BlockPos::getX).min().orElseThrow();
        int maxX = rods.stream().mapToInt(BlockPos::getX).max().orElseThrow();
        int minY = rods.stream().mapToInt(BlockPos::getY).min().orElseThrow();
        int maxY = rods.stream().mapToInt(BlockPos::getY).max().orElseThrow();
        int minZ = rods.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        int maxZ = rods.stream().mapToInt(BlockPos::getZ).max().orElseThrow();

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        double metal = rods.stream().mapToDouble(resistivity).average().orElse(1.0);
        Vec3 radiator = new Vec3((minX + maxX + 1) / 2.0, maxY + 1.0, (minZ + maxZ + 1) / 2.0);

        if (sizeY >= sizeX && sizeY >= sizeZ) {
            return new AntennaProfile(Shape.WHIP, sizeY, List.of(), List.of(), metal, radiator, AntennaDirection.DEFAULT);
        }

        Map<Integer, Integer> widthsX = widths(rods, Direction.Axis.X);
        Map<Integer, Integer> widthsZ = widths(rods, Direction.Axis.Z);
        boolean boomOnX = spaced(widthsX) == spaced(widthsZ) ? sizeX >= sizeZ : spaced(widthsX);
        Direction.Axis boom = boomOnX ? Direction.Axis.X : Direction.Axis.Z;
        Map<Integer, Integer> widths = boomOnX ? widthsX : widthsZ;

        List<Map.Entry<Integer, Integer>> rows = new ArrayList<>(widths.entrySet().stream().filter(row -> row.getValue() > 1).toList());
        if (rows.size() < 2) {
            AntennaDirection broadside = boom == Direction.Axis.X ? AntennaDirection.DEFAULT : AntennaDirection.of(90);
            return new AntennaProfile(Shape.DIPOLE, Math.max(sizeX, sizeZ), List.of(), List.of(), metal, radiator, broadside);
        }

        // Reflector first
        boolean reflectorFirst = rows.getFirst().getValue() >= rows.getLast().getValue();
        if (!reflectorFirst) {
            Collections.reverse(rows);
        }
        int start = rows.getFirst().getKey();
        List<Integer> elementWidths = rows.stream().map(Map.Entry::getValue).toList();
        List<Integer> positions = rows.stream().map(row -> Math.abs(row.getKey() - start)).toList();
        int degrees = boom == Direction.Axis.X ? (reflectorFirst ? 90 : 270) : (reflectorFirst ? 180 : 0);
        int longest = elementWidths.stream().mapToInt(Integer::intValue).max().orElseThrow();
        return new AntennaProfile(Shape.YAGI, longest, elementWidths, positions, metal, radiator, AntennaDirection.of(degrees));
    }

    private static Map<Integer, Integer> widths(Set<BlockPos> rods, Direction.Axis axis) {
        Map<Integer, Integer> widths = new TreeMap<>();
        for (BlockPos rod : rods) {
            widths.merge(rod.get(axis), 1, Integer::sum);
        }
        return widths;
    }

    private static boolean spaced(Map<Integer, Integer> widths) {
        Integer previous = null;
        for (Map.Entry<Integer, Integer> row : widths.entrySet()) {
            if (row.getValue() > 1) {
                if (previous != null && row.getKey() == previous + 1) {
                    return false;
                }
                previous = row.getKey();
            }
        }
        return true;
    }

    public int elements() {
        return this.elementWidths.size();
    }

    public Polarization polarization() {
        return this.shape == Shape.WHIP || this.shape == Shape.NONE ? Polarization.VERTICAL : Polarization.HORIZONTAL;
    }

    // Radiation efficiency
    public double efficiencyDb(double mhz) {
        double wavelength = LIGHT / mhz;
        return switch (this.shape) {
            case NONE, WHIP -> feedDb(2.0 * Math.PI * this.lengthBlocks / wavelength, 30.0, GROUND_OHMS, this.lengthBlocks, this.lengthBlocks, 60.0, mhz);
            case DIPOLE -> dipoleFeedDb(this.lengthBlocks, mhz);
            case YAGI -> dipoleFeedDb(yagi(mhz).drivenBlocks(), mhz);
        };
    }

    private double dipoleFeedDb(double lengthBlocks, double mhz) {
        return feedDb(Math.PI * lengthBlocks / (LIGHT / mhz), 60.0, JOINT_OHMS, lengthBlocks, lengthBlocks / 2.0, 120.0, mhz);
    }

    // Feed-point resistance and reactance with coil and tuner loss
    private double feedDb(double a, double rFactor, double lossOhms, double wireBlocks, double halfBlocks, double z0Factor, double mhz) {
        double sine = Math.sin(a);
        double sineSquared = Math.max(sine * sine, MIN_SIN_SQUARED);
        double radiation = rFactor * Wire.power(a) / sineSquared;
        double z0 = z0Factor * (Math.log(Math.max(2.0 * halfBlocks / ROD_RADIUS_BLOCKS, 4.0)) - 1.0);
        double reactance = z0 * Math.abs(Math.cos(a)) / Math.sqrt(sineSquared);
        double skin = Math.sqrt(Math.PI * mhz * 1.0E6 * MU_0 * COPPER_OHM_METRES * this.resistivity);
        double metal = skin * wireBlocks / (2.0 * Math.PI * ROD_RADIUS_BLOCKS) / 2.0 / sineSquared;
        double resistance = radiation + lossOhms + metal + reactance / COIL_Q;
        double loadedQ = Math.sqrt(Math.max(resistance, RADIO_OHMS) / Math.min(resistance, RADIO_OHMS) - 1.0);
        return 10.0 * Math.log10(radiation / resistance) - 10.0 * Math.log10(1.0 + loadedQ / TUNER_Q);
    }

    // Directivity along the ground
    public double directivityDb(double mhz, double dx, double dz) {
        return skyDirectivityDb(mhz, 0.0, dx, dz, Double.NaN);
    }

    // Directivity at an elevation
    public double skyDirectivityDb(double mhz, double elevationDeg, double dx, double dz, double heightBlocks) {
        double wavelength = LIGHT / mhz;
        double elevation = Math.toRadians(elevationDeg);
        double gain = switch (this.shape) {
            case NONE, WHIP -> {
                double a = 2.0 * Math.PI * this.lengthBlocks / wavelength;
                double field = Wire.field(a, Math.PI / 2.0 - elevation);
                yield 4.0 * field * field / Wire.power(a) / Math.pow(10.0, REAL_GROUND_DB / 10.0);
            }
            case DIPOLE -> dipoleGain(this.lengthBlocks, wavelength, elevation, dx, dz);
            case YAGI -> {
                Yagi yagi = yagi(mhz);
                if (!yagi.parasitic()) {
                    yield dipoleGain(yagi.drivenBlocks(), wavelength, elevation, dx, dz);
                }
                double peak = Math.pow(10.0, yagi.peakDb() / 10.0);
                yield Math.max(new CosinePattern(peak, yagi.halfAngleDeg()).gain(this.beam, dx, dz), peak * YAGI_BACK);
            }
        };
        if (this.shape != Shape.WHIP && this.shape != Shape.NONE && !Double.isNaN(heightBlocks)) {
            gain *= groundReflection(elevation, heightBlocks, wavelength);
        }
        return 10.0 * Math.log10(Math.max(gain, MIN_GAIN));
    }

    // Dipole directivity
    private double dipoleGain(double lengthBlocks, double wavelength, double elevation, double dx, double dz) {
        double a = Math.PI * lengthBlocks / wavelength;
        double distance = Math.hypot(dx, dz);
        double along = distance < 1.0E-6 ? 0.0 : (dx * -this.beam.dz() + dz * this.beam.dx()) / distance;
        double field = Wire.field(a, Math.acos(Math.clamp(along * Math.cos(elevation), -1.0, 1.0)));
        return 2.0 * field * field / Wire.power(a);
    }

    // Two-ray ground reflection
    private static double groundReflection(double elevation, double heightBlocks, double wavelength) {
        double phase = 4.0 * Math.PI * heightBlocks * Math.sin(elevation) / wavelength;
        return 1.0 + GROUND_REFLECTION * GROUND_REFLECTION - 2.0 * GROUND_REFLECTION * Math.cos(phase);
    }

    private record Yagi(boolean parasitic, double drivenBlocks, double peakDb, double halfAngleDeg) {}

    // Yagi coupling and gain and beamwidth
    private Yagi yagi(double mhz) {
        double wavelength = LIGHT / mhz;
        List<Integer> coupled = new ArrayList<>();
        for (int i = 0; i < this.elementWidths.size(); i++) {
            double waves = this.elementWidths.get(i) / wavelength;
            boolean resonant = waves >= MIN_ELEMENT_WAVES && waves <= MAX_ELEMENT_WAVES;
            if (!coupled.isEmpty() && (!resonant || this.elementPositions.get(i) - this.elementPositions.get(coupled.getLast()) > MAX_SPACING_WAVES * wavelength)) {
                break;
            }
            if (resonant) {
                coupled.add(i);
            }
        }

        if (coupled.size() < 2) {
            return new Yagi(false, this.lengthBlocks, DIPOLE_GAIN_DB, 90.0);
        }

        double boom = (this.elementPositions.get(coupled.getLast()) - this.elementPositions.get(coupled.getFirst())) / wavelength;
        double dbd = Math.min(YAGI_DBD_AT_REFERENCE + YAGI_DBD_PER_DECADE * Math.log10(boom / YAGI_REFERENCE_WAVES), 2.0 + 10.0 * Math.log10(coupled.size()));
        double peakDb = DIPOLE_GAIN_DB + Math.max(0.0, dbd);
        double halfPower = Math.toRadians(Math.sqrt(SPHERE_SQUARE_DEGREES / Math.pow(10.0, peakDb / 10.0))) / 2.0;
        double edge = Math.clamp((Math.cos(halfPower) - HALF_POWER) / (1.0 - HALF_POWER), -0.99, 0.99);
        double driven = coupled.stream().mapToInt(this.elementWidths::get).max().orElseThrow();
        return new Yagi(true, driven, peakDb, Math.toDegrees(Math.acos(edge)));
    }

    public LinkEnd linkEnd(double watts, double feedLossDb, double noiseFigureDb, double heightBlocks, double mhz, Vec3 toward) {
        double efficiency = efficiencyDb(mhz);
        double dx = toward.x - this.radiator.x;
        double dz = toward.z - this.radiator.z;
        return new LinkEnd(watts, heightBlocks, efficiency + directivityDb(mhz, dx, dz), efficiency, noiseFigureDb, feedLossDb, polarization(),
                elevation -> efficiency + skyDirectivityDb(mhz, elevation, dx, dz, heightBlocks));
    }
}
