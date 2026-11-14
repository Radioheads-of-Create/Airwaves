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
import java.util.stream.Collectors;

// lengthBlocks stem or driven element length
public record AntennaProfile(Shape shape, double lengthBlocks, TopHat hat, List<Integer> elementWidths, List<Integer> elementPositions, int driven,
                             double resistivity, Vec3 radiator, AntennaDirection beam) {
    public static final Codec<AntennaProfile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Shape.CODEC.fieldOf("shape").forGetter(AntennaProfile::shape),
            Codec.DOUBLE.fieldOf("length_blocks").forGetter(AntennaProfile::lengthBlocks),
            TopHat.CODEC.optionalFieldOf("hat", TopHat.NONE).forGetter(AntennaProfile::hat),
            Codec.INT.listOf().optionalFieldOf("element_widths", List.of()).forGetter(AntennaProfile::elementWidths),
            Codec.INT.listOf().optionalFieldOf("element_positions", List.of()).forGetter(AntennaProfile::elementPositions),
            Codec.INT.optionalFieldOf("driven", 0).forGetter(AntennaProfile::driven),
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
    // Top hat reach limit in radians
    private static final double MAX_HAT_RADIANS = 1.5;
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
        YAGI,
        TOP_LOADED;

        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static AntennaProfile bare(Vec3 position) {
        return new AntennaProfile(Shape.NONE, BARE_STUB_BLOCKS, TopHat.NONE, List.of(), List.of(), 0, 1.0, position, AntennaDirection.DEFAULT);
    }

    public static AntennaProfile whip(double heightBlocks, Vec3 radiator) {
        return new AntennaProfile(Shape.WHIP, heightBlocks, TopHat.NONE, List.of(), List.of(), 0, 1.0, radiator, AntennaDirection.DEFAULT);
    }

    public static AntennaProfile dipole(double lengthBlocks, Vec3 radiator, AntennaDirection broadside) {
        return dipole(lengthBlocks, 1.0, radiator, broadside);
    }

    private static AntennaProfile dipole(double lengthBlocks, double metal, Vec3 radiator, AntennaDirection broadside) {
        return new AntennaProfile(Shape.DIPOLE, lengthBlocks, TopHat.NONE, List.of(), List.of(), 0, metal, radiator, broadside);
    }

    // Tooltip profile
    public static AntennaProfile display(Shape shape, double lengthBlocks, int elements, Vec3 position) {
        return new AntennaProfile(shape, lengthBlocks, TopHat.NONE, Collections.nCopies(elements, 0), Collections.nCopies(elements, 0), 0, 1.0, position, AntennaDirection.DEFAULT);
    }

    // Shape from the feed rod on the Rod Base
    public static AntennaProfile of(Set<BlockPos> rods, BlockPos feed, ToDoubleFunction<BlockPos> resistivity) {
        double metal = rods.stream().mapToDouble(resistivity).average().orElse(1.0);
        int stem = 1 + run(rods, feed, Direction.UP);
        int runX = 1 + run(rods, feed, Direction.WEST) + run(rods, feed, Direction.EAST);
        int runZ = 1 + run(rods, feed, Direction.NORTH) + run(rods, feed, Direction.SOUTH);
        if (stem >= Math.max(runX, runZ)) {
            return monopole(rods, feed, stem, metal);
        }
        // Whip with an offset stem
        int height = span(rods, Direction.Axis.Y);
        if (height >= span(rods, Direction.Axis.X) && height >= span(rods, Direction.Axis.Z)) {
            return new AntennaProfile(Shape.WHIP, height, TopHat.NONE, List.of(), List.of(), 0, metal, top(rods, rods), AntennaDirection.DEFAULT);
        }
        return balanced(rods, feed, runX, runZ, metal);
    }

    // Monopole with a capacitance hat
    private static AntennaProfile monopole(Set<BlockPos> rods, BlockPos feed, int stem, double metal) {
        int top = feed.getY() + stem - 1;
        int maxY = top;
        int wire = 0;
        int radius = 0;
        for (BlockPos rod : rods) {
            int reach = Math.max(Math.abs(rod.getX() - feed.getX()), Math.abs(rod.getZ() - feed.getZ()));
            maxY = Math.max(maxY, rod.getY());
            if (reach > 0 && rod.getY() >= top) {
                wire++;
                radius = Math.max(radius, reach);
            }
        }

        Vec3 radiator = new Vec3(feed.getX() + 0.5, maxY + 1.0, feed.getZ() + 0.5);
        if (wire == 0) {
            return new AntennaProfile(Shape.WHIP, stem, TopHat.NONE, List.of(), List.of(), 0, metal, radiator, AntennaDirection.DEFAULT);
        }
        return new AntennaProfile(Shape.TOP_LOADED, stem, new TopHat(wire, radius), List.of(), List.of(), 0, metal, radiator, AntennaDirection.DEFAULT);
    }

    // Centre-fed dipole or Yagi
    private static AntennaProfile balanced(Set<BlockPos> rods, BlockPos feed, int runX, int runZ, double metal) {
        Set<BlockPos> level = rods.stream().filter(rod -> rod.getY() == feed.getY()).collect(Collectors.toSet());
        int sizeX = span(level, Direction.Axis.X);
        int sizeZ = span(level, Direction.Axis.Z);
        Vec3 radiator = top(level, rods);

        Map<Integer, Integer> widthsX = widths(level, Direction.Axis.X);
        Map<Integer, Integer> widthsZ = widths(level, Direction.Axis.Z);
        boolean boomOnX = spaced(widthsX) == spaced(widthsZ) ? sizeX >= sizeZ : spaced(widthsX);
        Direction.Axis boom = boomOnX ? Direction.Axis.X : Direction.Axis.Z;
        // Broadside directions
        AntennaDirection alongBoom = boomOnX ? AntennaDirection.DEFAULT : AntennaDirection.of(90);
        AntennaDirection acrossBoom = boomOnX ? AntennaDirection.of(90) : AntennaDirection.DEFAULT;

        List<Map.Entry<Integer, Integer>> rows = new ArrayList<>((boomOnX ? widthsX : widthsZ).entrySet().stream().filter(row -> row.getValue() > 1).toList());
        if (rows.size() < 2) {
            return dipole(Math.max(sizeX, sizeZ), metal, radiator, alongBoom);
        }

        int fed = -1;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getKey() == feed.get(boom)) {
                fed = i;
            }
        }
        // Fed on the bare boom
        if (fed < 0) {
            return dipole(boomOnX ? runX : runZ, metal, radiator, alongBoom);
        }

        // Beam toward the shorter elements
        int drivenWidth = rows.get(fed).getValue();
        int forward = 0;
        for (int i = 0; i < rows.size(); i++) {
            forward += Integer.signum(i - fed) * Integer.signum(drivenWidth - rows.get(i).getValue());
        }
        if (forward == 0) {
            forward = Integer.signum(rows.getFirst().getValue() - rows.getLast().getValue());
        }
        if (forward == 0) {
            return dipole(drivenWidth, metal, radiator, acrossBoom);
        }

        // Reflector end first
        if (forward < 0) {
            Collections.reverse(rows);
            fed = rows.size() - 1 - fed;
        }
        int start = rows.getFirst().getKey();
        List<Integer> elementWidths = rows.stream().map(Map.Entry::getValue).toList();
        List<Integer> positions = rows.stream().map(row -> Math.abs(row.getKey() - start)).toList();
        int degrees = boomOnX ? (forward > 0 ? 90 : 270) : (forward > 0 ? 180 : 0);
        return new AntennaProfile(Shape.YAGI, drivenWidth, TopHat.NONE, elementWidths, positions, fed, metal, radiator, AntennaDirection.of(degrees));
    }

    private static int span(Set<BlockPos> rods, Direction.Axis axis) {
        IntSummaryStatistics range = rods.stream().mapToInt(rod -> rod.get(axis)).summaryStatistics();
        return range.getMax() - range.getMin() + 1;
    }

    // Radiator point
    private static Vec3 top(Set<BlockPos> plan, Set<BlockPos> rods) {
        IntSummaryStatistics x = plan.stream().mapToInt(BlockPos::getX).summaryStatistics();
        IntSummaryStatistics z = plan.stream().mapToInt(BlockPos::getZ).summaryStatistics();
        int maxY = rods.stream().mapToInt(BlockPos::getY).max().orElseThrow();
        return new Vec3((x.getMin() + x.getMax() + 1) / 2.0, maxY + 1.0, (z.getMin() + z.getMax() + 1) / 2.0);
    }

    // Straight run length
    private static int run(Set<BlockPos> rods, BlockPos from, Direction direction) {
        int length = 0;
        for (BlockPos next = from.relative(direction); rods.contains(next); next = next.relative(direction)) {
            length++;
        }
        return length;
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

    // Fed against ground
    private boolean upright() {
        return this.shape == Shape.NONE || this.shape == Shape.WHIP || this.shape == Shape.TOP_LOADED;
    }

    public Polarization polarization() {
        return upright() ? Polarization.VERTICAL : Polarization.HORIZONTAL;
    }

    // Radiation efficiency
    public double efficiencyDb(double mhz) {
        double wavelength = LIGHT / mhz;
        if (upright()) {
            double a = 2.0 * Math.PI * this.lengthBlocks / wavelength;
            double b = hatRadians(wavelength);
            return feedDb(a + b, Wire.power(a, b), 30.0, GROUND_OHMS, this.lengthBlocks, this.lengthBlocks, 60.0, mhz);
        }
        double a = Math.PI * this.lengthBlocks / wavelength;
        return feedDb(a, Wire.power(a), 60.0, JOINT_OHMS, this.lengthBlocks, this.lengthBlocks / 2.0, 120.0, mhz);
    }

    // Top hat electrical length
    private double hatRadians(double wavelength) {
        if (!this.hat.present()) {
            return 0.0;
        }
        double disc = 4.0 / Math.PI * (Math.log(Math.max(2.0 * this.lengthBlocks / ROD_RADIUS_BLOCKS, 4.0)) - 1.0);
        double arms = Math.clamp(this.hat.wireBlocks() / this.hat.radiusBlocks(), 1.0, Math.max(disc, 1.0));
        double reach = Math.min(2.0 * Math.PI * this.hat.radiusBlocks() / wavelength, MAX_HAT_RADIANS);
        return Math.atan(arms * Math.tan(reach));
    }

    // Feed-point resistance and reactance with coil and tuner loss
    private double feedDb(double feed, double power, double rFactor, double lossOhms, double wireBlocks, double halfBlocks, double z0Factor, double mhz) {
        double sine = Math.sin(feed);
        double sineSquared = Math.max(sine * sine, MIN_SIN_SQUARED);
        double radiation = rFactor * power / sineSquared;
        double z0 = z0Factor * (Math.log(Math.max(2.0 * halfBlocks / ROD_RADIUS_BLOCKS, 4.0)) - 1.0);
        double reactance = z0 * Math.abs(Math.cos(feed)) / Math.sqrt(sineSquared);
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
            case NONE, WHIP, TOP_LOADED -> {
                double a = 2.0 * Math.PI * this.lengthBlocks / wavelength;
                double b = hatRadians(wavelength);
                double field = Wire.field(a, b, Math.PI / 2.0 - elevation);
                yield 4.0 * field * field / Wire.power(a, b) / Math.pow(10.0, REAL_GROUND_DB / 10.0);
            }
            case DIPOLE -> dipoleGain(wavelength, elevation, dx, dz);
            case YAGI -> {
                Yagi yagi = yagi(wavelength);
                if (!yagi.parasitic()) {
                    yield dipoleGain(wavelength, elevation, dx, dz);
                }
                double peak = Math.pow(10.0, yagi.peakDb() / 10.0);
                yield Math.max(new CosinePattern(peak, yagi.halfAngleDeg()).gain(this.beam, dx, dz), peak * YAGI_BACK);
            }
        };
        if (!upright() && !Double.isNaN(heightBlocks)) {
            gain *= groundReflection(elevation, heightBlocks, wavelength);
        }
        return 10.0 * Math.log10(Math.max(gain, MIN_GAIN));
    }

    // Dipole directivity
    private double dipoleGain(double wavelength, double elevation, double dx, double dz) {
        double a = Math.PI * this.lengthBlocks / wavelength;
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

    private record Yagi(boolean parasitic, double peakDb, double halfAngleDeg) {
        private static final Yagi DIPOLE = new Yagi(false, DIPOLE_GAIN_DB, 90.0);
    }

    // Yagi coupling and gain and beamwidth
    private Yagi yagi(double wavelength) {
        if (this.driven < 0 || this.driven >= this.elementWidths.size() || !resonant(this.driven, wavelength)) {
            return Yagi.DIPOLE;
        }

        // Directors
        int last = this.driven;
        for (int i = this.driven + 1; i < this.elementWidths.size(); i++) {
            if (!resonant(i, wavelength) || this.elementWidths.get(i) > this.elementWidths.get(this.driven) || !coupled(last, i, wavelength)) {
                break;
            }
            last = i;
        }
        // Reflector
        int first = this.driven;
        if (this.driven > 0 && resonant(this.driven - 1, wavelength) && this.elementWidths.get(this.driven - 1) >= this.elementWidths.get(this.driven)
                && coupled(this.driven - 1, this.driven, wavelength)) {
            first = this.driven - 1;
        }

        int elements = last - first + 1;
        if (elements < 2) {
            return Yagi.DIPOLE;
        }

        double boom = (this.elementPositions.get(last) - this.elementPositions.get(first)) / wavelength;
        double dbd = Math.min(YAGI_DBD_AT_REFERENCE + YAGI_DBD_PER_DECADE * Math.log10(boom / YAGI_REFERENCE_WAVES), 2.0 + 10.0 * Math.log10(elements));
        double peakDb = DIPOLE_GAIN_DB + Math.max(0.0, dbd);
        double halfPower = Math.toRadians(Math.sqrt(SPHERE_SQUARE_DEGREES / Math.pow(10.0, peakDb / 10.0))) / 2.0;
        double edge = Math.clamp((Math.cos(halfPower) - HALF_POWER) / (1.0 - HALF_POWER), -0.99, 0.99);
        return new Yagi(true, peakDb, Math.toDegrees(Math.acos(edge)));
    }

    private boolean resonant(int element, double wavelength) {
        double waves = this.elementWidths.get(element) / wavelength;
        return waves >= MIN_ELEMENT_WAVES && waves <= MAX_ELEMENT_WAVES;
    }

    private boolean coupled(int back, int front, double wavelength) {
        return this.elementPositions.get(front) - this.elementPositions.get(back) <= MAX_SPACING_WAVES * wavelength;
    }

    public LinkEnd linkEnd(double watts, double feedLossDb, double noiseFigureDb, double heightBlocks, double mhz, Vec3 toward) {
        double efficiency = efficiencyDb(mhz);
        double dx = toward.x - this.radiator.x;
        double dz = toward.z - this.radiator.z;
        return new LinkEnd(watts, heightBlocks, efficiency + directivityDb(mhz, dx, dz), efficiency, noiseFigureDb, feedLossDb, polarization(),
                elevation -> efficiency + skyDirectivityDb(mhz, elevation, dx, dz, heightBlocks));
    }
}
