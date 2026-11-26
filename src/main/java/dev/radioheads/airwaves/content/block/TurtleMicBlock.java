package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class TurtleMicBlock extends AbstractMicrophoneBlock {
    public static final MapCodec<TurtleMicBlock> CODEC = simpleCodec(TurtleMicBlock::new);

    // Model pixels with the front facing north
    public static final Vec3 CAPSULE = new Vec3(8.0, 9.89, 9.96);
    public static final Vec3 SOCKET = new Vec3(8.0, 2.0, 9);
    public static final Vec3 KNOB = new Vec3(8.0, 3.0, 3.0);
    private static final float[][] BOXES = {
            {5.0F, 0.0F, 5.0F, 11.0F, 1.0F, 11.0F},
            {7.0F, 1.0F, 7.0F, 9.0F, 5.0F, 9.0F},
            {2.0F, 5.0F, 7.0F, 14.0F, 6.0F, 9.0F},
            {2.0F, 6.0F, 7.0F, 3.0F, 12.0F, 9.0F},
            {13.0F, 6.0F, 7.0F, 14.0F, 12.0F, 9.0F},
            {3.0F, 10.3F, 7.3F, 13.0F, 11.7F, 8.7F},
            {5.0F, 6.5F, 4.6F, 11.0F, 16.0F, 11.4F}
    };

    public TurtleMicBlock(Properties properties) {
        super(properties, CAPSULE, SOCKET, KNOB, BOXES);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    // Knob and the stand around it
    public boolean onKnob(BlockState state, BlockPos pos, Vec3 hit) {
        Vec3 model = HfTransceiverPanel.toModel(state.getValue(FACING), pos, hit);
        return Math.abs(model.x - KNOB.x) <= 2.5 && Math.abs(model.y - KNOB.y) <= 2.0 && model.z <= 9.0;
    }
}
