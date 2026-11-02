package dev.radioheads.airwaves.content.block;

import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.content.entity.block.RodBaseBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@NothingNullByDefault
public class RodBaseBlock extends Block implements IBE<RodBaseBlockEntity> {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0),
            Block.box(4.0, 1.0, 4.0, 12.0, 11.0, 12.0),
            Block.box(6.0, 11.0, 6.0, 10.0, 16.0, 10.0)
    );

    public RodBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            Cables.broken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<RodBaseBlockEntity> getBlockEntityClass() {
        return RodBaseBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RodBaseBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.ROD_BASE.get();
    }
}
