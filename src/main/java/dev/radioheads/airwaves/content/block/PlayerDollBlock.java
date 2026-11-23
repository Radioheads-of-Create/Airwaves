package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class PlayerDollBlock extends Block {
    public static final MapCodec<PlayerDollBlock> CODEC = simpleCodec(PlayerDollBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final float[][] BOXES = {
            {5.0F, 0.0F, 5.0F, 11.0F, 1.0F, 11.0F},
            {7.0F, 1.0F, 7.0F, 9.0F, 5.0F, 9.0F},
            {2.0F, 5.0F, 7.0F, 14.0F, 6.0F, 9.0F},
            {2.0F, 6.0F, 7.0F, 3.0F, 12.0F, 9.0F},
            {13.0F, 6.0F, 7.0F, 14.0F, 12.0F, 9.0F},
            {3.0F, 10.3F, 7.3F, 13.0F, 11.7F, 8.7F},
            {5.0F, 6.5F, 4.6F, 11.0F, 16.0F, 11.4F}
    };
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    @Override
    protected MapCodec<? extends PlayerDollBlock> codec() {
        return CODEC;
    }

    public PlayerDollBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return (BlockState)state.setValue(FACING, rot.rotate((Direction)state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.computeIfAbsent(state.getValue(FACING), PlayerDollBlock::shape);
    }

    private static VoxelShape shape(Direction facing) {
        VoxelShape shape = Shapes.empty();
        float[][] shapes = {
                {5.0F, 0.0F, 5.0F, 11.0F, 1.0F, 11.0F},
                {7.0F, 1.0F, 7.0F, 9.0F, 5.0F, 9.0F},
                {2.0F, 5.0F, 7.0F, 14.0F, 6.0F, 9.0F},
                {2.0F, 6.0F, 7.0F, 3.0F, 12.0F, 9.0F},
                {13.0F, 6.0F, 7.0F, 14.0F, 12.0F, 9.0F},
                {3.0F, 10.3F, 7.3F, 13.0F, 11.7F, 8.7F},
                {5.0F, 6.5F, 4.6F, 11.0F, 16.0F, 11.4F}
        };
        for (float[] box : shapes) {
            Vec3 a = HfTransceiverPanel.toBlock(facing, box[0], box[1], box[2]);
            Vec3 b = HfTransceiverPanel.toBlock(facing, box[3], box[4], box[5]);
            shape = Shapes.or(shape, Shapes.box(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z), Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z)));
        }
        return shape.optimize();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
