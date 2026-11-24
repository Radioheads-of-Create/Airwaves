package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.integration.sable.blocks.CableNodeSable;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

// Aimed cable picking
public final class CableAim {
    // Pick radius in blocks
    public static final double RADIUS = 0.25;
    private static final double MAX_SAG = 2.4;
    private static final double EPSILON = 1.0E-9;

    private CableAim() {}

    // Aimed cable
    public record Target(BlockPos from, BlockPos to, Vec3 fromAnchor, Vec3 toAnchor, CableType type, double distance) {
        public int length(Level level) {
            return Cables.length(level, this.from, this.to);
        }

        public int spools(Level level) {
            return Cables.spools(length(level));
        }
    }

    // Parabolic sag point at t
    public static Vec3 along(Vec3 from, Vec3 delta, double t) {
        double sag = Math.min(MAX_SAG, 0.045 * delta.length() + 0.06);
        return from.add(delta.scale(t)).subtract(0.0, sag * 4.0 * t * (1.0 - t), 0.0);
    }

    private static int pieces(double length) {
        return Math.clamp((int) (length * 2.0), 8, 128);
    }

    // Ray hit distance
    public static OptionalDouble hit(Vec3 eye, Vec3 end, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double range = end.subtract(eye).length();
        int pieces = pieces(delta.length());
        double best = Double.MAX_VALUE;
        Vec3 previous = from;
        for (int i = 1; i <= pieces; i++) {
            Vec3 point = along(from, delta, (double) i / pieces);
            double[] st = closest(eye, end, previous, point);
            Vec3 onLook = eye.add(end.subtract(eye).scale(st[0]));
            Vec3 onCable = previous.add(point.subtract(previous).scale(st[1]));
            if (onLook.distanceTo(onCable) <= RADIUS) {
                best = Math.min(best, st[0] * range);
            }
            previous = point;
        }
        return best == Double.MAX_VALUE ? OptionalDouble.empty() : OptionalDouble.of(best);
    }

    // Point to cable distance
    public static double distance(Vec3 point, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        int pieces = pieces(delta.length());
        double best = Double.MAX_VALUE;
        Vec3 previous = from;
        for (int i = 1; i <= pieces; i++) {
            Vec3 next = along(from, delta, (double) i / pieces);
            double[] st = closest(point, point, previous, next);
            best = Math.min(best, point.distanceTo(previous.add(next.subtract(previous).scale(st[1]))));
            previous = next;
        }
        return best;
    }

    // Segment to segment closest points
    static double[] closest(Vec3 p1, Vec3 q1, Vec3 p2, Vec3 q2) {
        Vec3 d1 = q1.subtract(p1);
        Vec3 d2 = q2.subtract(p2);
        Vec3 r = p1.subtract(p2);
        double a = d1.dot(d1);
        double e = d2.dot(d2);
        double f = d2.dot(r);
        if (a <= EPSILON && e <= EPSILON) {
            return new double[] {0.0, 0.0};
        }
        if (a <= EPSILON) {
            return new double[] {0.0, Math.clamp(f / e, 0.0, 1.0)};
        }
        double c = d1.dot(r);
        if (e <= EPSILON) {
            return new double[] {Math.clamp(-c / a, 0.0, 1.0), 0.0};
        }
        double b = d1.dot(d2);
        double denominator = a * e - b * b;
        double s = denominator > EPSILON ? Math.clamp((b * f - c * e) / denominator, 0.0, 1.0) : 0.0;
        double t = (b * s + f) / e;
        if (t < 0.0) {
            return new double[] {Math.clamp(-c / a, 0.0, 1.0), 0.0};
        }
        if (t > 1.0) {
            return new double[] {Math.clamp((b - c) / a, 0.0, 1.0), 1.0};
        }
        return new double[] {s, t};
    }

    // Nearest cable along the look
    public static Optional<Target> aimed(Level level, Vec3 eye, Vec3 look, double range) {
        return aimed(level, null, eye, look, range);
    }

    public static Optional<Target> aimed(Level level, @Nullable Entity viewer, Vec3 eye, Vec3 look, double range) {
        Vec3 end = eye.add(look.normalize().scale(range));
        // Search radius
        double reach = range + Cables.MAX_LENGTH / 2.0 + MAX_SAG + 1.0;
        Set<Pair> seen = new HashSet<>();
        Target best = search(level, eye.x, eye.z, reach, eye, end, seen, null);

        if (viewer != null) {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getTrackingSubLevel(viewer);
            if (subLevel == null) {
                subLevel = SableCompanion.INSTANCE.getContaining(level, eye);
            }
            if (subLevel == null) {
                subLevel = SableCompanion.INSTANCE.getLastTrackingSubLevel(viewer);
            }
            if (subLevel != null) {
                Vec3 local = subLevel.logicalPose().transformPositionInverse(eye);
                if (local.x != eye.x || local.z != eye.z) {
                    best = search(level, local.x, local.z, reach, eye, end, seen, best);
                }
            }
        }
        return Optional.ofNullable(best);
    }

    @Nullable
    private static Target search(Level level, double centerX, double centerZ, double reach, Vec3 eye, Vec3 end, Set<Pair> seen, @Nullable Target best) {
        int minX = SectionPos.blockToSectionCoord(centerX - reach);
        int maxX = SectionPos.blockToSectionCoord(centerX + reach);
        int minZ = SectionPos.blockToSectionCoord(centerZ - reach);
        int maxZ = SectionPos.blockToSectionCoord(centerZ + reach);
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof CableNode node && !blockEntity.isRemoved()) {
                        best = nearer(level, blockEntity.getBlockPos(), node, eye, end, seen, best);
                    }
                }
            }
        }
        return best;
    }

    @Nullable
    private static Target nearer(Level level, BlockPos origin, CableNode node, Vec3 eye, Vec3 end, Set<Pair> seen, @Nullable Target best) {
        for (CableLink link : node.links().all()) {
            CableNode other = Cables.node(level, link.other());
            if (other == null || !seen.add(pair(origin, link.other()))) {
                continue;
            }
            Vec3 from = CableNodeSable.toGlobal(level, node, node.anchor());
            Vec3 to = CableNodeSable.toGlobal(level, other, other.anchor());
            OptionalDouble hit = hit(eye, end, from, to);
            if (hit.isPresent() && (best == null || hit.getAsDouble() < best.distance())) {
                best = new Target(origin, link.other(), from, to, link.type(), hit.getAsDouble());
            }
        }
        return best;
    }

    private record Pair(long low, long high) {}

    // Unordered cable key
    private static Pair pair(BlockPos a, BlockPos b) {
        long x = a.asLong();
        long y = b.asLong();
        return new Pair(Math.min(x, y), Math.max(x, y));
    }
}
