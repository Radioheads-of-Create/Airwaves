package dev.radioheads.airwaves.network;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.entity.block.MicrophoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class Microphones {
    public static final double RANGE = 2.0;

    private static final Map<ResourceKey<Level>, Set<MicrophoneBlockEntity>> LOADED = new HashMap<>();

    private Microphones() {}

    public static void add(Level level, MicrophoneBlockEntity microphone) {
        LOADED.computeIfAbsent(level.dimension(), key -> new LinkedHashSet<>()).add(microphone);
    }

    public static void remove(Level level, MicrophoneBlockEntity microphone) {
        Set<MicrophoneBlockEntity> microphones = LOADED.get(level.dimension());
        if (microphones != null) {
            microphones.remove(microphone);
        }
    }

    public static Set<MicrophoneBlockEntity> in(Level level) {
        return LOADED.getOrDefault(level.dimension(), Set.of());
    }

    // Switched on cabled microphones that hear a sound nearest first
    public static List<MicrophoneBlockEntity> hearing(Level level, Vec3 sound, BlockPos source) {
        List<MicrophoneBlockEntity> hearing = new ArrayList<>();
        for (MicrophoneBlockEntity microphone : in(level)) {
            if (microphone.on() && microphone.gain() > 0.0F && hears(shapes(level), microphone.capsule(), microphone.getBlockPos(), sound, source)
                    && microphone.station().isPresent()) {
                hearing.add(microphone);
            }
        }
        hearing.sort(Comparator.comparingDouble(microphone -> microphone.capsule().distanceToSqr(sound)));
        return hearing;
    }

    public static Function<BlockPos, VoxelShape> shapes(BlockGetter level) {
        return pos -> level.getBlockState(pos).getCollisionShape(level, pos);
    }

    // Range and line of sight past the source and microphone blocks
    public static boolean hears(Function<BlockPos, VoxelShape> shapes, Vec3 capsule, BlockPos microphone, Vec3 sound, BlockPos source) {
        return sound.closerThan(capsule, RANGE) && clear(shapes, sound, capsule, List.of(microphone, source));
    }

    // Line of sight through collision shapes
    public static boolean clear(Function<BlockPos, VoxelShape> shapes, Vec3 from, Vec3 to, Collection<BlockPos> ignored) {
        return BlockGetter.traverseBlocks(from, to, shapes, (blocks, pos) -> {
            if (ignored.contains(pos)) {
                return null;
            }
            return blocks.apply(pos).clip(from, to, pos) != null ? Boolean.FALSE : null;
        }, blocks -> Boolean.TRUE);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LOADED.clear();
    }
}
