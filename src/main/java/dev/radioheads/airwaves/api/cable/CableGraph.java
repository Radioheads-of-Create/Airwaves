package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Cable network per level
@NothingNullByDefault
public final class CableGraph extends SavedData {
    private static final String NAME = "airwaves_cables";
    private static final SavedData.Factory<CableGraph> FACTORY = new SavedData.Factory<>(CableGraph::new, CableGraph::load, null);

    // Line type per device
    private final Map<Long, LineType> nodes = new HashMap<>();
    private final Map<Long, Map<Long, Edge>> edges = new HashMap<>();
    // Telephone network per node
    private final Map<Long, Integer> networks = new HashMap<>();
    // TX or RX per insulator
    private final Map<Long, LineSide> sides = new HashMap<>();

    // Flow length and call delay in ms
    public record Edge(CableLink.Flow flow, double length, double delayMs) {}

    private record Step(long pos, double delayMs) {}

    public static CableGraph of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public void link(BlockPos a, LineType lineA, BlockPos b, LineType lineB, double length, CableType type) {
        long from = a.asLong();
        long to = b.asLong();
        Set<Long> sideA = component(from);
        Integer networkA = networkOf(sideA);
        Integer networkB = sideA.contains(to) ? networkA : networkOf(component(to));

        this.nodes.put(from, lineA);
        this.nodes.put(to, lineB);
        put(from, to, new Edge(CableLink.Flow.OUT, length, length * type.delayMsPerBlock()));

        // Network number merge
        boolean merged = networkA != null && networkB != null && !networkA.equals(networkB);
        Integer network = merged ? Integer.valueOf(fresh(RandomSource.create())) : networkA != null ? networkA : networkB;
        Set<Long> joined = component(from);
        if (network != null && lineOf(joined) == LineType.TELEPHONE) {
            joined.forEach(pos -> this.networks.put(pos, network));
        }

        retag(joined);
        setDirty();
    }

    private record Reach(long pos, double length, LineSide side) {}

    // Insulator sides from the nearest jack
    // Side tagging by nearest jack
    private void retag(Set<Long> component) {
        PriorityQueue<Reach> queue = new PriorityQueue<>(Comparator.comparingDouble(Reach::length).thenComparingInt(this::tie));
        for (long pos : component) {
            if (this.nodes.get(pos) != LineType.TELEPHONE) {
                continue;
            }
            for (Map.Entry<Long, Edge> edge : this.edges.getOrDefault(pos, Map.of()).entrySet()) {
                CableLink.Flow flow = edge.getValue().flow();
                if (this.nodes.get(edge.getKey()) == LineType.NONE && flow != CableLink.Flow.BOTH) {
                    queue.add(new Reach(edge.getKey(), edge.getValue().length(), flow == CableLink.Flow.OUT ? LineSide.TX : LineSide.RX));
                }
            }
        }
        Map<Long, LineSide> found = new HashMap<>();
        while (!queue.isEmpty()) {
            Reach tag = queue.poll();
            if (found.putIfAbsent(tag.pos(), tag.side()) != null) {
                continue;
            }
            for (Map.Entry<Long, Edge> edge : this.edges.getOrDefault(tag.pos(), Map.of()).entrySet()) {
                if (this.nodes.get(edge.getKey()) == LineType.NONE && !found.containsKey(edge.getKey())) {
                    queue.add(new Reach(edge.getKey(), tag.length() + edge.getValue().length(), tag.side()));
                }
            }
        }
        for (long pos : component) {
            LineSide side = found.get(pos);
            if (side == null) {
                this.sides.remove(pos);
            } else {
                this.sides.put(pos, side);
            }
        }
    }

    // Tie break
    private int tie(Reach tag) {
        return (tag.side() == sideAt(tag.pos()) ? 0 : LineSide.values().length) + tag.side().ordinal();
    }

    private void retagAll() {
        Map<Long, LineSide> before = new HashMap<>(this.sides);
        Set<Long> done = new HashSet<>();
        for (long pos : new ArrayList<>(this.nodes.keySet())) {
            if (!done.contains(pos)) {
                Set<Long> component = component(pos);
                done.addAll(component);
                retag(component);
            }
        }
        if (!before.equals(this.sides)) {
            setDirty();
        }
    }

    public LineSide sideAt(BlockPos pos) {
        return sideAt(pos.asLong());
    }

    private LineSide sideAt(long pos) {
        return this.sides.getOrDefault(pos, LineSide.NONE);
    }

    public void unlink(BlockPos a, BlockPos b) {
        long from = a.asLong();
        long to = b.asLong();
        drop(from, to);
        drop(to, from);
        // Side retag
        retag(component(from));
        retag(component(to));
        setDirty();
    }

    public void remove(BlockPos pos) {
        long at = pos.asLong();
        Map<Long, Edge> out = this.edges.remove(at);
        if (out != null) {
            out.keySet().forEach(other -> drop(other, at));
            out.keySet().forEach(other -> retag(component(other)));
        }
        forget(at);
        setDirty();
    }

    // Both ends of a cable
    private void put(long from, long to, Edge edge) {
        this.edges.computeIfAbsent(from, pos -> new HashMap<>()).put(to, edge);
        this.edges.computeIfAbsent(to, pos -> new HashMap<>()).put(from, new Edge(edge.flow().reverse(), edge.length(), edge.delayMs()));
    }

    private void drop(long from, long to) {
        Map<Long, Edge> out = this.edges.get(from);
        if (out != null) {
            out.remove(to);
            if (out.isEmpty()) {
                this.edges.remove(from);
                forget(from);
            }
        }
    }

    private void forget(long pos) {
        this.nodes.remove(pos);
        this.networks.remove(pos);
        this.sides.remove(pos);
    }

    // Loaded node sync into the graph
    public void sync(BlockPos pos, CableNode node) {
        long at = pos.asLong();
        boolean changed = false;
        for (CableLink link : node.links().all()) {
            long other = link.other().asLong();
            Edge edge = this.edges.getOrDefault(at, Map.of()).get(other);
            if (edge == null) {
                double length = Math.sqrt(pos.distSqr(link.other()));
                put(at, other, new Edge(link.flow(), length, length * link.type().delayMsPerBlock()));
                this.nodes.putIfAbsent(other, LineType.NONE);
                changed = true;
            } else if (edge.delayMs() != edge.length() * link.type().delayMsPerBlock()) {
                put(at, other, new Edge(edge.flow(), edge.length(), edge.length() * link.type().delayMsPerBlock()));
                changed = true;
            }
        }
        if (this.edges.containsKey(at) && this.nodes.get(at) != node.line()) {
            this.nodes.put(at, node.line());
            changed = true;
        }
        if (changed) {
            setDirty();
        }
    }

    public boolean linked(BlockPos a, BlockPos b) {
        return this.edges.getOrDefault(a.asLong(), Map.of()).containsKey(b.asLong());
    }

    public Set<Long> component(long start) {
        Set<Long> seen = new HashSet<>(List.of(start));
        Deque<Long> queue = new ArrayDeque<>(List.of(start));
        while (!queue.isEmpty()) {
            for (long next : this.edges.getOrDefault(queue.poll(), Map.of()).keySet()) {
                if (seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return seen;
    }

    public LineType lineOf(Collection<Long> component) {
        LineType line = LineType.NONE;
        for (long pos : component) {
            LineType own = this.nodes.getOrDefault(pos, LineType.NONE);
            if (own == LineType.TELEPHONE) {
                return own;
            }
            if (own == LineType.RADIO) {
                line = own;
            }
        }
        return line;
    }

    public LineType lineAt(BlockPos pos) {
        return lineOf(component(pos.asLong()));
    }

    @Nullable
    private Integer networkOf(Collection<Long> component) {
        for (long pos : component) {
            Integer network = this.networks.get(pos);
            if (network != null) {
                return network;
            }
        }
        return null;
    }

    // Line network number
    public int network(BlockPos pos, RandomSource random) {
        Set<Long> component = component(pos.asLong());
        Integer network = networkOf(component);
        int number = network != null ? network : fresh(random);
        component.forEach(node -> this.networks.put(node, number));
        setDirty();
        return number;
    }

    // Free two digit network number
    private int fresh(RandomSource random) {
        Set<Integer> used = new HashSet<>(this.networks.values());
        List<Integer> free = new ArrayList<>();
        for (int tens = 1; tens <= 8; tens++) {
            for (int ones = 1; ones <= 8; ones++) {
                if (!used.contains(tens * 10 + ones)) {
                    free.add(tens * 10 + ones);
                }
            }
        }
        return free.isEmpty() ? (1 + random.nextInt(8)) * 10 + 1 + random.nextInt(8) : free.get(random.nextInt(free.size()));
    }

    // Dijkstra least call delay in ms
    public OptionalDouble route(BlockPos from, BlockPos to) {
        long start = from.asLong();
        long goal = to.asLong();
        Map<Long, Double> best = new HashMap<>(Map.of(start, 0.0));
        PriorityQueue<Step> queue = new PriorityQueue<>((x, y) -> Double.compare(x.delayMs(), y.delayMs()));
        queue.add(new Step(start, 0.0));
        while (!queue.isEmpty()) {
            Step step = queue.poll();
            if (step.delayMs() > best.getOrDefault(step.pos(), Double.MAX_VALUE)) {
                continue;
            }
            if (step.pos() == goal) {
                return OptionalDouble.of(step.delayMs());
            }
            // Route stop at telephones
            if (step.pos() != start && this.nodes.get(step.pos()) == LineType.TELEPHONE) {
                continue;
            }
            for (Map.Entry<Long, Edge> edge : this.edges.getOrDefault(step.pos(), Map.of()).entrySet()) {
                double delayMs = step.delayMs() + edge.getValue().delayMs();
                boolean jack = step.pos() == start || this.nodes.get(edge.getKey()) == LineType.TELEPHONE;
                if ((!jack || edge.getValue().flow().sends()) && delayMs < best.getOrDefault(edge.getKey(), Double.MAX_VALUE)) {
                    best.put(edge.getKey(), delayMs);
                    queue.add(new Step(edge.getKey(), delayMs));
                }
            }
        }
        return OptionalDouble.empty();
    }

    public static CableGraph load(CompoundTag tag, HolderLookup.Provider registries) {
        CableGraph graph = new CableGraph();
        long[] positions = tag.getLongArray("Nodes");
        byte[] lines = tag.getByteArray("Lines");
        for (int i = 0; i < positions.length; i++) {
            graph.nodes.put(positions[i], LineType.of(i < lines.length ? lines[i] : 0));
        }
        CableLink.Flow[] flows = CableLink.Flow.values();
        for (Tag entry : tag.getList("Edges", Tag.TAG_COMPOUND)) {
            CompoundTag edge = (CompoundTag) entry;
            long from = edge.getLong("From");
            long to = edge.getLong("To");
            CableLink.Flow flow = flows[Math.floorMod(edge.getByte("Flow"), flows.length)];
            double length = edge.getDouble("Length");
            // Old save copper delay
            double delayMs = edge.contains("Delay") ? edge.getDouble("Delay") : length * CableType.COPPER_DELAY_MS_PER_BLOCK;
            graph.put(from, to, new Edge(flow, length, delayMs));
        }
        long[] numbered = tag.getLongArray("Networked");
        int[] numbers = tag.getIntArray("Networks");
        for (int i = 0; i < Math.min(numbered.length, numbers.length); i++) {
            graph.networks.put(numbered[i], numbers[i]);
        }
        long[] sided = tag.getLongArray("Sided");
        byte[] sides = tag.getByteArray("Sides");
        for (int i = 0; i < Math.min(sided.length, sides.length); i++) {
            graph.sides.put(sided[i], LineSide.of(sides[i]));
        }
        // Old save side retag
        graph.retagAll();
        return graph;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] positions = new long[this.nodes.size()];
        byte[] lines = new byte[this.nodes.size()];
        int i = 0;
        for (Map.Entry<Long, LineType> node : this.nodes.entrySet()) {
            positions[i] = node.getKey();
            lines[i++] = (byte) node.getValue().ordinal();
        }
        tag.putLongArray("Nodes", positions);
        tag.putByteArray("Lines", lines);

        ListTag list = new ListTag();
        for (Map.Entry<Long, Map<Long, Edge>> from : this.edges.entrySet()) {
            for (Map.Entry<Long, Edge> to : from.getValue().entrySet()) {
                if (from.getKey() < to.getKey()) {
                    CompoundTag edge = new CompoundTag();
                    edge.putLong("From", from.getKey());
                    edge.putLong("To", to.getKey());
                    edge.putByte("Flow", (byte) to.getValue().flow().ordinal());
                    edge.putDouble("Length", to.getValue().length());
                    edge.putDouble("Delay", to.getValue().delayMs());
                    list.add(edge);
                }
            }
        }
        tag.put("Edges", list);

        long[] numbered = new long[this.networks.size()];
        int[] numbers = new int[this.networks.size()];
        i = 0;
        for (Map.Entry<Long, Integer> network : this.networks.entrySet()) {
            numbered[i] = network.getKey();
            numbers[i++] = network.getValue();
        }
        tag.putLongArray("Networked", numbered);
        tag.putIntArray("Networks", numbers);

        long[] sided = new long[this.sides.size()];
        byte[] sides = new byte[this.sides.size()];
        i = 0;
        for (Map.Entry<Long, LineSide> side : this.sides.entrySet()) {
            sided[i] = side.getKey();
            sides[i++] = (byte) side.getValue().ordinal();
        }
        tag.putLongArray("Sided", sided);
        tag.putByteArray("Sides", sides);
        return tag;
    }
}
