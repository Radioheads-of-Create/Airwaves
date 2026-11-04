package dev.radioheads.airwaves.api.link;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

// World terrain around a link
public record RadioTerrain(Heights heights, Water water, Cover cover) {
    private static final int[] SITE_RADII = {32, 64, 128};
    private static final int SITE_DIRECTIONS = 8;
    private static final int PATH_SPACING = 8;
    private static final int MAX_PATH_SAMPLES = 128;
    // Blocks skipped at each end of a path
    private static final double NEAR_BLOCKS = 2.0;
    private static final int MAX_COVER = 32;

    @FunctionalInterface
    public interface Heights {
        double at(int x, int z);
    }

    @FunctionalInterface
    public interface Water {
        boolean at(int x, int z);
    }

    @FunctionalInterface
    public interface Cover {
        int over(Vec3 antenna);
    }

    public record Path(double obstructionDb, double seaFraction) {}

    public RadioTerrain(Heights heights) {
        this(heights, (x, z) -> false, antenna -> 0);
    }

    public static RadioTerrain of(Level level) {
        return new RadioTerrain(
                (x, z) -> {
                    int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    return height > level.getMinBuildHeight() ? height : Double.NaN;
                },
                (x, z) -> level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) > level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z),
                antenna -> coverOver(level, antenna));
    }

    // Cover over an antenna
    private static int coverOver(Level level, Vec3 antenna) {
        int x = Mth.floor(antenna.x);
        int z = Mth.floor(antenna.z);
        if (!level.hasChunk(x >> 4, z >> 4)) {
            return 0;
        }

        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int count = 0;
        for (int y = Mth.floor(antenna.y) + 1; y < top && count < MAX_COVER; y++) {
            BlockState state = level.getBlockState(pos.set(x, y, z));
            if ((state.blocksMotion() && !state.is(BlockTags.LEAVES)) || !state.getFluidState().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // Height above average terrain
    public double siteHeight(Vec3 antenna) {
        OptionalDouble ground = loadedGround(antenna).average();
        return ground.isPresent() ? Math.max(antenna.y - ground.getAsDouble(), 1.0) : 1.0;
    }

    public int siteSamples(Vec3 antenna) {
        return (int) loadedGround(antenna).count();
    }

    private DoubleStream loadedGround(Vec3 antenna) {
        int x = Mth.floor(antenna.x);
        int z = Mth.floor(antenna.z);
        DoubleStream.Builder ground = DoubleStream.builder();
        ground.add(heights.at(x, z));

        for (int radius : SITE_RADII) {
            for (int i = 0; i < SITE_DIRECTIONS; i++) {
                double angle = Mth.TWO_PI * i / SITE_DIRECTIONS;
                ground.add(heights.at(x + Mth.floor(radius * Math.cos(angle)), z + Mth.floor(radius * Math.sin(angle))));
            }
        }

        return ground.build().filter(height -> !Double.isNaN(height));
    }

    public int cover(Vec3 antenna) {
        return this.cover.over(antenna);
    }

    public double obstructionDb(double mhz, Vec3 from, Vec3 to) {
        return path(mhz, from, to).obstructionDb();
    }

    // Path profile for ridge loss and sea share
    public Path path(double mhz, Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double length = Math.hypot(dx, dz);
        int samples = Math.min(MAX_PATH_SAMPLES, Mth.floor(length / PATH_SPACING));
        double worst = 0.0;
        int counted = 0;
        int wet = 0;

        for (int i = 1; i < samples; i++) {
            double t = (double) i / samples;
            double along = t * length;
            if (along < NEAR_BLOCKS || length - along < NEAR_BLOCKS) {
                continue;
            }

            int x = Mth.floor(from.x + dx * t);
            int z = Mth.floor(from.z + dz * t);
            double ground = heights.at(x, z);
            if (Double.isNaN(ground)) {
                continue;
            }

            counted++;
            if (water.at(x, z)) {
                wet++;
            }
            double line = Mth.lerp(t, from.y, to.y);
            if (ground > line) {
                worst = Math.max(worst, Propagation.knifeEdgeDb(mhz, ground - line, along, length - along));
            }
        }

        return new Path(worst, counted == 0 ? 0.0 : (double) wet / counted);
    }
}
