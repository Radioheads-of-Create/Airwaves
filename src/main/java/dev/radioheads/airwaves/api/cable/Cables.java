package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Cables {
    public static final int MAX_LENGTH = 64;
    public static final int BLOCKS_PER_SPOOL = 8;
    private static final int MAX_NODES = 512;

    public enum Refusal {
        NO_JUNCTION,
        WRONG_LINE,
        TOO_FAR,
        ALREADY_LINKED,
        NO_SOCKET
    }

    public record Feed(AntennaProfile antenna, FeedLine line) {
        public double lossDb(double mhz) {
            return this.line.lossDb(mhz);
        }

        public double amplifierDb() {
            return this.line.amplifierDb();
        }
    }

    public record Survey(Optional<Feed> feed, boolean complete) {}

    // Antenna side restarts at each powered amplifier
    private record Hop(BlockPos pos, double factor, double blocks, double sideFactor, double sideBlocks, double amplifierDb) {}

    private Cables() {}

    @Nullable
    public static CableNode node(Level level, BlockPos pos) {
        if (level.isLoaded(pos)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CableNode node) {
                return node;
            }
        }
        return null;
    }

    public static int length(BlockPos a, BlockPos b) {
        return (int) Math.round(Math.sqrt(a.distSqr(b)));
    }

    public static int spools(int length) {
        return Math.ceilDiv(length, BLOCKS_PER_SPOOL);
    }

    // Flow from a to b
    public static Optional<Refusal> refusal(CableNode a, BlockPos posA, CableNode b, BlockPos posB) {
        if (!a.junction() && !b.junction()) {
            return Optional.of(Refusal.NO_JUNCTION);
        }
        if (a.wiredLine().clashes(b.wiredLine())) {
            return Optional.of(Refusal.WRONG_LINE);
        }
        if (length(posA, posB) > MAX_LENGTH) {
            return Optional.of(Refusal.TOO_FAR);
        }
        if (a.links().has(posB)) {
            return Optional.of(Refusal.ALREADY_LINKED);
        }
        if (!a.accepts(CableLink.Flow.OUT) || !b.accepts(CableLink.Flow.IN)) {
            return Optional.of(Refusal.NO_SOCKET);
        }
        return Optional.empty();
    }

    public static boolean canLink(CableNode a, BlockPos posA, CableNode b, BlockPos posB) {
        return !posA.equals(posB) && refusal(a, posA, b, posB).isEmpty();
    }

    public static boolean link(Level level, BlockPos posA, BlockPos posB, CableType type) {
        CableNode a = node(level, posA);
        CableNode b = node(level, posB);
        if (a == null || b == null || !canLink(a, posA, b, posB)) {
            return false;
        }

        a.links().add(posB, type, CableLink.Flow.OUT);
        b.links().add(posA, type, CableLink.Flow.IN);
        if (level instanceof ServerLevel server) {
            CableGraph.of(server).link(posA, a.line(), posB, b.line(), a.anchor().distanceTo(b.anchor()), type);
            rewire(server, List.of(posA));
        }
        a.cablesChanged();
        b.cablesChanged();
        return true;
    }

    public static List<ItemStack> unlink(Level level, BlockPos pos) {
        List<ItemStack> cables = new ArrayList<>();
        CableNode node = node(level, pos);
        if (node == null) {
            return cables;
        }

        List<BlockPos> touched = new ArrayList<>(List.of(pos));
        for (CableLink link : node.links().all()) {
            CableNode other = node(level, link.other());
            if (other != null && other.links().remove(pos)) {
                other.cablesChanged();
            }
            if (level instanceof ServerLevel server) {
                CableGraph.of(server).unlink(pos, link.other());
            }
            touched.add(link.other());
            cables.add(link.type().cable(spools(length(pos, link.other()))));
        }

        node.links().all().clear();
        if (level instanceof ServerLevel server) {
            rewire(server, touched);
        }
        node.cablesChanged();
        return cables;
    }

    // Cable cut with spool refund
    public static Optional<ItemStack> cut(Level level, BlockPos a, BlockPos b) {
        CableNode nodeA = node(level, a);
        CableNode nodeB = node(level, b);
        Optional<CableLink> link = linkBetween(nodeA, b).or(() -> linkBetween(nodeB, a));
        if (link.isEmpty()) {
            return Optional.empty();
        }

        boolean changedA = nodeA != null && nodeA.links().remove(b);
        boolean changedB = nodeB != null && nodeB.links().remove(a);
        if (level instanceof ServerLevel server) {
            CableGraph.of(server).unlink(a, b);
            rewire(server, List.of(a, b));
        }
        if (changedA) {
            nodeA.cablesChanged();
        }
        if (changedB) {
            nodeB.cablesChanged();
        }
        return Optional.of(link.get().type().cable(spools(length(a, b))));
    }

    private static Optional<CableLink> linkBetween(@Nullable CableNode node, BlockPos other) {
        return node == null ? Optional.empty() : node.links().all().stream().filter(link -> link.other().equals(other)).findFirst();
    }

    // Line type update for loaded nodes
    public static void rewire(ServerLevel level, Collection<BlockPos> touched) {
        CableGraph graph = CableGraph.of(level);
        Set<Long> done = new HashSet<>();
        for (BlockPos pos : touched) {
            if (done.contains(pos.asLong())) {
                continue;
            }
            Set<Long> component = graph.component(pos.asLong());
            done.addAll(component);
            LineType line = graph.lineOf(component);
            for (long at : component) {
                BlockPos node = BlockPos.of(at);
                if (level.isLoaded(node) && level.getBlockEntity(node) instanceof CableNode loaded) {
                    loaded.lineChanged(line, graph.sideAt(node));
                }
            }
        }
    }

    // Graph sync on load
    public static void loaded(ServerLevel level, BlockPos pos, CableNode node) {
        CableGraph graph = CableGraph.of(level);
        boolean changed = true;
        if (node.links().graphed()) {
            // Missed cuts
            changed = node.links().all().removeIf(link -> !graph.linked(pos, link.other()));
        }
        node.links().graphed(true);
        graph.sync(pos, node);
        node.lineChanged(graph.lineAt(pos), graph.sideAt(pos));
        if (changed) {
            node.cablesChanged();
        }
    }

    public static void broken(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        for (ItemStack cable : unlink(level, pos)) {
            level.addFreshEntity(new ItemEntity(level, center.x, center.y, center.z, cable));
        }
    }

    // Devices past the junctions nearest first
    public static List<BlockPos> devices(Function<BlockPos, CableNode> nodes, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>(Set.of(start));
        Deque<BlockPos> queue = new ArrayDeque<>(List.of(start));
        List<BlockPos> devices = new ArrayList<>();

        while (!queue.isEmpty() && seen.size() < MAX_NODES) {
            CableNode node = nodes.apply(queue.poll());
            if (node == null) {
                continue;
            }

            for (CableLink link : node.links().all()) {
                CableNode next = seen.add(link.other()) ? nodes.apply(link.other()) : null;
                if (next == null) {
                    continue;
                }
                if (next.junction()) {
                    queue.add(link.other());
                } else {
                    devices.add(link.other());
                }
            }
        }

        return devices;
    }

    public static Survey survey(Function<BlockPos, CableNode> nodes, Predicate<BlockPos> loaded, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>(Set.of(start));
        Deque<Hop> queue = new ArrayDeque<>();
        queue.add(new Hop(start, 0.0, 0.0, 0.0, 0.0, 0.0));
        boolean complete = true;

        while (!queue.isEmpty() && seen.size() < MAX_NODES) {
            Hop hop = queue.poll();
            CableNode node = nodes.apply(hop.pos());
            if (node == null) {
                continue;
            }

            for (CableLink link : node.links().all()) {
                complete &= loaded.test(link.other());
                CableNode next = seen.add(link.other()) ? nodes.apply(link.other()) : null;
                if (next == null) {
                    continue;
                }

                double length = node.anchor().distanceTo(next.anchor());
                double segment = link.type().lossPerBlock() * length;
                boolean amplifier = next.amplifierDb() > 0.0;
                Hop reached = new Hop(link.other(), hop.factor() + segment, hop.blocks() + length,
                        amplifier ? 0.0 : hop.sideFactor() + segment, amplifier ? 0.0 : hop.sideBlocks() + length,
                        Math.max(hop.amplifierDb(), next.amplifierDb()));
                boolean settled = next.settled();
                Optional<AntennaProfile> antenna = settled ? next.feeds() : Optional.empty();
                if (antenna.isPresent()) {
                    FeedLine line = new FeedLine(reached.factor(), reached.blocks(), reached.sideFactor(), reached.sideBlocks(), reached.amplifierDb());
                    return new Survey(Optional.of(new Feed(antenna.get(), line)), true);
                }
                complete &= settled;
                if (next.junction()) {
                    queue.add(reached);
                }
            }
        }

        return new Survey(Optional.empty(), complete);
    }
}
