package dev.radioheads.airwaves.api.audio;

import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.network.Microphones;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public interface AudioRecorder {
    static void register(Level level, AudioRecorder recorder) {
        Microphones.add(level, recorder);
    }

    static void unregister(Level level, AudioRecorder recorder) {
        Microphones.remove(level, recorder);
    }

    UUID id();

    Vec3 capsule();

    BlockPos getBlockPos();

    boolean recording();

    float gain();

    Optional<RadioStation> station();

    default boolean active() {
        return recording() && gain() > 0.0F && station().isPresent();
    }
}
