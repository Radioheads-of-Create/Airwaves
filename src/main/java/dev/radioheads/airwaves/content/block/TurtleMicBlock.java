package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.content.entity.block.MicrophoneBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

@NothingNullByDefault
public class TurtleMicBlock extends HorizontalDirectionalBlock implements IBE<MicrophoneBlockEntity> {
    public static final MapCodec<TurtleMicBlock> CODEC = simpleCodec(TurtleMicBlock::new);
    public static final BooleanProperty ON_AIR = BooleanProperty.create("on_air");

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
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    public TurtleMicBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ON_AIR, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON_AIR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.computeIfAbsent(state.getValue(FACING), TurtleMicBlock::shape);
    }

    private static VoxelShape shape(Direction facing) {
        VoxelShape shape = Shapes.empty();
        for (float[] box : BOXES) {
            Vec3 a = HfTransceiverPanel.toBlock(facing, box[0], box[1], box[2]);
            Vec3 b = HfTransceiverPanel.toBlock(facing, box[3], box[4], box[5]);
            shape = Shapes.or(shape, Shapes.box(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z), Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z)));
        }
        return shape.optimize();
    }

    // Knob and the stand around it
    public static boolean onKnob(BlockState state, BlockPos pos, Vec3 hit) {
        Vec3 model = HfTransceiverPanel.toModel(state.getValue(FACING), pos, hit);
        return Math.abs(model.x - KNOB.x) <= 2.5 && Math.abs(model.y - KNOB.y) <= 2.0 && model.z <= 9.0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            Cables.broken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<MicrophoneBlockEntity> getBlockEntityClass() {
        return MicrophoneBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MicrophoneBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.MICROPHONE.get();
    }
}
