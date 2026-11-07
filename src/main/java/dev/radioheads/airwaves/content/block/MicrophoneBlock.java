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
public class MicrophoneBlock extends HorizontalDirectionalBlock implements IBE<MicrophoneBlockEntity> {
    public static final MapCodec<MicrophoneBlock> CODEC = simpleCodec(MicrophoneBlock::new);
    public static final BooleanProperty ON_AIR = BooleanProperty.create("on_air");

    // Model pixels with the front facing north
    public static final Vec3 CAPSULE = new Vec3(8.0, 9.89, 9.96);
    public static final Vec3 SOCKET = new Vec3(8.0, 2.0, 9.25);
    public static final Vec3 KNOB = new Vec3(8.0, 3.0, 6.75);
    private static final float[][] BOXES = {
            {4.0F, 0.0F, 5.0F, 12.0F, 1.1F, 11.0F},
            {6.25F, 1.0F, 6.5F, 9.75F, 6.75F, 9.25F},
            {5.5F, 5.9F, 6.8F, 10.5F, 13.9F, 13.2F}
    };
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    public MicrophoneBlock(Properties properties) {
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
        return SHAPES.computeIfAbsent(state.getValue(FACING), MicrophoneBlock::shape);
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
