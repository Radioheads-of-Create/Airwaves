package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.util.Vec3Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class MicrophoneBlock extends AbstractMicrophoneBlock {
    public static final MapCodec<MicrophoneBlock> CODEC = simpleCodec(MicrophoneBlock::new);

    // Model pixels with the front facing north
    public static final Vec3 CAPSULE = new Vec3(8.0, 9.89, 9.96);
    public static final Vec3 SOCKET = new Vec3(8.0, 2.0, 9.25);
    public static final Vec3 KNOB = new Vec3(8.0, 3.0, 6.75);
    private static final float[][] BOXES = {
            {4.0F, 0.0F, 5.0F, 12.0F, 1.1F, 11.0F},
            {6.25F, 1.0F, 6.5F, 9.75F, 6.75F, 9.25F},
            {5.5F, 5.9F, 6.8F, 10.5F, 13.9F, 13.2F}
    };

    public MicrophoneBlock(Properties properties) {
        super(properties, CAPSULE, SOCKET, KNOB, BOXES);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    // Knob and the stand around it
    public boolean onKnob(BlockState state, BlockPos pos, Vec3 hit) {
        Vec3 model = Vec3Util.toModel(state.getValue(FACING), pos, hit);
        return Math.abs(model.x - KNOB.x) <= 2.5 && Math.abs(model.y - KNOB.y) <= 2.0 && model.z <= 9.0;
    }
}
