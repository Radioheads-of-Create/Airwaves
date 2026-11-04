package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@NothingNullByDefault
public class AmplifierBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<AmplifierBlock> CODEC = simpleCodec(AmplifierBlock::new);

    public AmplifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
