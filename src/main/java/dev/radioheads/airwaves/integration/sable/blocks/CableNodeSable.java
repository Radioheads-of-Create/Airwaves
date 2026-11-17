package dev.radioheads.airwaves.integration.sable.blocks;

import dev.radioheads.airwaves.api.cable.CableNode;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;

import javax.annotation.Nullable;

public class CableNodeSable {
    @Nullable
    private static BlockEntity cachedBlockEntity;
    private static long cachedTick = Long.MIN_VALUE;
    @Nullable
    private static SubLevelAccess cachedSubLevel;

    public static BlockPos getActualPosition(Level level, BlockPos pos) {
        SubLevelAccess subLevel = getNodeSubLevel(level, pos);
        if (subLevel == null) {
            return pos;
        }
        Vec3 transformed = subLevel.logicalPose().transformPosition(pos.getCenter());
        return BlockPos.containing(transformed.x, transformed.y, transformed.z);
    }

    public static BlockPos getActualPosition(Level level, CableNode node) {
        BlockPos nodePos = node.getBlockPos();
        SubLevelAccess subLevel = getNodeSubLevel(level, node);
        if (subLevel == null) {
            return nodePos;
        }
        Vec3 transformed = subLevel.logicalPose().transformPosition(nodePos.getCenter());
        return BlockPos.containing(transformed.x, transformed.y, transformed.z);
    }

    public static Quaterniond getSublevelRot(Level level, CableNode node) {
        SubLevelAccess subLevel = getNodeSubLevel(level, node);
        if (subLevel == null) return new Quaterniond();
        return new Quaterniond(subLevel.logicalPose().orientation());
    }

    public static Vec3 getActualCenter(Level level, CableNode node) {
        Vec3 center = node.getBlockPos().getCenter();
        SubLevelAccess subLevel = getNodeSubLevel(level, node);
        if (subLevel == null) {
            return center;
        }
        return subLevel.logicalPose().transformPosition(center);
    }

    @Nullable
    private static SubLevelAccess getNodeSubLevel(Level level, CableNode node) {
        if (!(node instanceof BlockEntity blockEntity)) {
            return null;
        }
        return getBlockEntitySubLevel(level, blockEntity);
    }

    @Nullable
    private static SubLevelAccess getNodeSubLevel(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        return getBlockEntitySubLevel(level, blockEntity);
    }

    @Nullable
    private static SubLevelAccess getBlockEntitySubLevel(Level level, BlockEntity blockEntity) {
        long tick = level != null ? level.getGameTime() : -1;
        if (blockEntity != cachedBlockEntity || tick != cachedTick) {
            cachedBlockEntity = blockEntity;
            cachedTick = tick;
            cachedSubLevel = SableCompanion.INSTANCE.getContaining(blockEntity);
        }
        return cachedSubLevel;
    }
}