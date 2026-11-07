package dev.radioheads.airwaves.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class HfTransceiverPanel {
    private static final double FRONT_TOLERANCE = 0.1;
    private static final long[][] BANDS = {
            {3_500_000L, 4_000_000L, 3_750_000L}, {7_000_000L, 7_300_000L, 7_100_000L}, {10_100_000L, 10_150_000L, 10_125_000L},
            {14_000_000L, 14_350_000L, 14_195_000L}, {18_068_000L, 18_168_000L, 18_130_000L}, {21_000_000L, 21_450_000L, 21_250_000L},
            {24_890_000L, 24_990_000L, 24_950_000L}, {28_000_000L, 29_700_000L, 28_450_000L}
    };
    private static final int[] METRES = {80, 40, 30, 20, 17, 15, 12, 10};

    private HfTransceiverPanel() {}

    public static Vec3 toBlock(Direction facing, double modelX, double modelY, double modelZ) {
        Direction left = facing.getClockWise();
        double x = 0.5 + (left.getStepX() * (modelX - 8.0) + facing.getStepX() * (8.0 - modelZ)) / 16.0;
        double z = 0.5 + (left.getStepZ() * (modelX - 8.0) + facing.getStepZ() * (8.0 - modelZ)) / 16.0;
        return new Vec3(x, modelY / 16.0, z);
    }

    // Block hit to model pixels
    public static Vec3 toModel(Direction facing, BlockPos pos, Vec3 hit) {
        Direction left = facing.getClockWise();
        double dx = hit.x - (pos.getX() + 0.5);
        double dz = hit.z - (pos.getZ() + 0.5);
        return new Vec3(8.0 + 16.0 * (dx * left.getStepX() + dz * left.getStepZ()), 16.0 * (hit.y - pos.getY()),
                8.0 - 16.0 * (dx * facing.getStepX() + dz * facing.getStepZ()));
    }

    public static float modelRotation(Direction facing) {
        return (facing.toYRot() + 180.0F) % 360.0F;
    }

    @Nullable
    public static HfTransceiverControl controlAt(Direction facing, BlockPos pos, Vec3 hit) {
        Direction left = facing.getClockWise();
        double dx = hit.x - (pos.getX() + 0.5);
        double dz = hit.z - (pos.getZ() + 0.5);
        double panelX = 8.0 - 16.0 * (dx * left.getStepX() + dz * left.getStepZ());
        double panelY = HfTransceiverControl.PANEL_HEIGHT - 16.0 * (hit.y - pos.getY());
        double depth = 8.0 - 16.0 * (dx * facing.getStepX() + dz * facing.getStepZ());
        if (depth > HfTransceiverControl.FRONT_Z + FRONT_TOLERANCE) {
            return null;
        }
        return HfTransceiverControl.at(panelX, panelY);
    }

    public static int bandOf(long hertz) {
        for (int i = 0; i < BANDS.length; i++) {
            if (hertz >= BANDS[i][0] && hertz <= BANDS[i][1]) {
                return i;
            }
        }
        return -1;
    }

    public static int metres(int band) {
        return METRES[band];
    }

    public static long[] defaultBandMemory() {
        long[] memory = new long[BANDS.length];
        for (int i = 0; i < BANDS.length; i++) {
            memory[i] = BANDS[i][2];
        }
        return memory;
    }

    public static long jump(long hertz, int direction, boolean megahertz, long[] memory) {
        if (megahertz) {
            return hertz + direction * 1_000_000L;
        }
        int band = bandOf(hertz);
        int next;
        if (band >= 0) {
            next = Math.floorMod(band + direction, BANDS.length);
        } else if (direction > 0) {
            next = 0;
            while (next < BANDS.length && BANDS[next][0] <= hertz) {
                next++;
            }
            next %= BANDS.length;
        } else {
            next = BANDS.length - 1;
            while (next > 0 && BANDS[next][1] >= hertz) {
                next--;
            }
        }
        return memory[next];
    }

    public static int powerStep(int value, int watts, int steps, int ratedWatts) {
        int step = Math.max(1, Math.round(Math.abs(watts) * steps / (float) ratedWatts));
        int next = watts > 0 ? (value < step ? step : value + step) : value - step;
        return Math.clamp(next, 1, steps);
    }
}
