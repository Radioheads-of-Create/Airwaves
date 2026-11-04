package dev.radioheads.airwaves.content.block;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.content.entity.block.InsulatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@NothingNullByDefault
public class InsulatorBlock extends Block implements IBE<InsulatorBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShaper SHAPE = new AllShapes.Builder(Block.box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0))
            .add(6.0, 2.0, 6.0, 10.0, 9.0, 10.0)
            .add(7.0, 9.0, 7.0, 9.0, 10.0, 9.0)
            .forDirectional();

    public InsulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            Cables.broken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<InsulatorBlockEntity> getBlockEntityClass() {
        return InsulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends InsulatorBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.INSULATOR.get();
    }
}
