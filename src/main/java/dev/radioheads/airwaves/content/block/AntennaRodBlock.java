package dev.radioheads.airwaves.content.block;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@NothingNullByDefault
public class AntennaRodBlock extends RotatedPillarBlock {
    public static final Map<Direction, BooleanProperty> LINKS = PipeBlock.PROPERTY_BY_DIRECTION;

    private static final int MAX_RODS = 256;
    private static final VoxelShape SHAPE_X = Block.box(0.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape SHAPE_Y = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape SHAPE_Z = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 16.0);
    private static final Map<Direction, VoxelShape> ARMS = Map.of(
            Direction.DOWN, Block.box(6.0, 0.0, 6.0, 10.0, 8.0, 10.0),
            Direction.UP, Block.box(6.0, 8.0, 6.0, 10.0, 16.0, 10.0),
            Direction.NORTH, Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 8.0),
            Direction.SOUTH, Block.box(6.0, 6.0, 8.0, 10.0, 10.0, 16.0),
            Direction.WEST, Block.box(0.0, 6.0, 6.0, 8.0, 10.0, 10.0),
            Direction.EAST, Block.box(8.0, 6.0, 6.0, 16.0, 10.0, 10.0)
    );

    private final double resistivity;
    private final Map<BlockState, VoxelShape> shapes;

    // Resistivity relative to copper
    public AntennaRodBlock(Properties properties, double resistivity) {
        super(properties);
        this.resistivity = resistivity;

        BlockState state = defaultBlockState();
        for (BooleanProperty link : LINKS.values()) {
            state = state.setValue(link, false);
        }
        registerDefaultState(state);
        this.shapes = getShapeForEachState(AntennaRodBlock::shapeOf);
    }

    public double resistivity() {
        return this.resistivity;
    }

    public static Set<BlockPos> connectedTo(Level level, BlockPos base) {
        Set<BlockPos> rods = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        BlockPos top = base.above();
        if (level.isLoaded(top) && level.getBlockState(top).getBlock() instanceof AntennaRodBlock) {
            rods.add(top);
            queue.add(top);
        }

        while (!queue.isEmpty() && rods.size() < MAX_RODS) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!rods.contains(next) && level.isLoaded(next) && level.getBlockState(next).getBlock() instanceof AntennaRodBlock) {
                    rods.add(next);
                    queue.add(next);
                }
            }
        }

        return rods;
    }

    public static boolean fullyLoaded(Level level, BlockPos base) {
        return Stream.concat(Stream.of(base), connectedTo(level, base).stream())
                .allMatch(pos -> Direction.Plane.HORIZONTAL.stream().allMatch(direction -> level.isLoaded(pos.relative(direction))));
    }

    private static BlockState linked(BlockState state, LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos next = pos.relative(direction);
            BlockState neighbor = level.getBlockState(next);
            boolean base = direction == Direction.DOWN && neighbor.getBlock() instanceof RodBaseBlock;
            boolean mounted = direction.getAxis() == state.getValue(AXIS) && neighbor.isFaceSturdy(level, next, direction.getOpposite());

            state = state.setValue(LINKS.get(direction), base || mounted || neighbor.getBlock() instanceof AntennaRodBlock);
        }
        return state;
    }

    private static VoxelShape shapeOf(BlockState state) {
        VoxelShape shape = switch (state.getValue(AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
        for (Direction direction : Direction.values()) {
            if (state.getValue(LINKS.get(direction))) {
                shape = Shapes.or(shape, ARMS.get(direction));
            }
        }
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        LINKS.values().forEach(builder::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return linked(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return linked(state, level, pos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = super.rotate(state, rotation);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            rotated = rotated.setValue(LINKS.get(rotation.rotate(side)), state.getValue(LINKS.get(side)));
        }
        return rotated;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            mirrored = mirrored.setValue(LINKS.get(mirror.mirror(side)), state.getValue(LINKS.get(side)));
        }
        return mirrored;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }
}
