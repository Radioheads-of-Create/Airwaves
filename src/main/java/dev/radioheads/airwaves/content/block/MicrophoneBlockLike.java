package dev.radioheads.airwaves.content.block;

import dev.radioheads.airwaves.api.util.Vec3Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MicrophoneBlockLike {
    Direction facing(BlockState state);

    Vec3 capsule();

    Vec3 socket();

    @Nullable Vec3 knob();

    default boolean onKnob(BlockState state, BlockPos pos, Vec3 hit) {
        Vec3 knob = knob();
        if (knob == null) {
            return false;
        }
        Vec3 model = Vec3Util.toModel(facing(state), pos, hit);
        return Math.abs(model.x - knob.x) <= 2.5 && Math.abs(model.y - knob.y) <= 2.0 && model.z <= 9.0;
    }

    @Nullable
    static MicrophoneBlockLike of(BlockState state) {
        return state.getBlock() instanceof MicrophoneBlockLike microphone ? microphone : null;
    }
}
