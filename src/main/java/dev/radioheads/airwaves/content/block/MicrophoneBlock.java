package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class MicrophoneBlock extends AbstractMicrophoneBlock {
    public static final MapCodec<MicrophoneBlock> CODEC = simpleCodec(MicrophoneBlock::new);

    // Model pixels with the front facing north
    public static final Vec3 CAPSULE = new Vec3(8.0, 9.89, 9.96);
    public static final Vec3 SOCKET = new Vec3(8.0, 2.0, 9.25);
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
}
