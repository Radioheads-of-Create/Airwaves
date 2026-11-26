package dev.radioheads.airwaves.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class Vec3Util {
    private Vec3Util() {}

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
}
