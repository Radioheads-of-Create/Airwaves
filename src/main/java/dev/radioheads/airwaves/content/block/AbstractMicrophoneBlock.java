package dev.radioheads.airwaves.content.block;

import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.util.Vec3Util;
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
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public abstract class AbstractMicrophoneBlock extends HorizontalDirectionalBlock implements MicrophoneBlockLike, IBE<MicrophoneBlockEntity> {
    public static final BooleanProperty ON_AIR = BooleanProperty.create("on_air");
    protected final Vec3 capsule;
    protected final Vec3 socket;
    @Nullable
    protected final Vec3 knob;
    private final float[][] boxes;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    protected AbstractMicrophoneBlock(Properties properties, Vec3 capsule, Vec3 socket, @Nullable Vec3 knob, float[][] boxes) {
        super(properties);
        this.capsule = capsule;
        this.socket = socket;
        this.knob = knob;
        this.boxes = boxes;
        registerDefaultState(defaultBlockState().setValue(ON_AIR, false));
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
        return this.shapes.computeIfAbsent(state.getValue(FACING), this::shape);
    }

    private VoxelShape shape(Direction facing) {
        VoxelShape shape = Shapes.empty();
        for (float[] box : this.boxes) {
            Vec3 a = Vec3Util.toBlock(facing, box[0], box[1], box[2]);
            Vec3 b = Vec3Util.toBlock(facing, box[3], box[4], box[5]);
            shape = Shapes.or(shape, Shapes.box(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z),
                    Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z)));
        }
        return shape.optimize();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            Cables.broken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Direction facing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public Vec3 capsule() {
        return this.capsule;
    }

    @Override
    public Vec3 socket() {
        return this.socket;
    }

    @Override
    @Nullable
    public Vec3 knob() {
        return this.knob;
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
