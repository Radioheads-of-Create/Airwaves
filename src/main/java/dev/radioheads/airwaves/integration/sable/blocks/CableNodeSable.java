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

    public static Vec3 toGlobal(Level level, CableNode node, Vec3 local) {
        SubLevelAccess subLevel = getNodeSubLevel(level, node);
        return subLevel == null ? local : subLevel.logicalPose().transformPosition(local);
    }

    public static Vec3 toLocal(Level level, CableNode source, Vec3 global) {
        SubLevelAccess subLevel = getNodeSubLevel(level, source);
        return subLevel == null ? global : subLevel.logicalPose().transformPositionInverse(global);
    }

    public static Vec3 toLocal(Level level, CableNode source, CableNode target) {
        return toLocal(level, source, toGlobal(level, target, target.anchor()));
    }

    public static boolean inSubLevel(Level level, CableNode node) {
        return getNodeSubLevel(level, node) != null;
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