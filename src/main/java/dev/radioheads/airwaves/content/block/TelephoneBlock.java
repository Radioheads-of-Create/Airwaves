package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.content.entity.block.TelephoneBlockEntity;
import dev.radioheads.airwaves.content.item.TelephoneEarpieceItem;
import dev.radioheads.airwaves.network.telephone.Telephones;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

@NothingNullByDefault
public class TelephoneBlock extends HorizontalDirectionalBlock implements IBE<TelephoneBlockEntity> {
    public static final MapCodec<TelephoneBlock> CODEC = simpleCodec(TelephoneBlock::new);
    // Earpiece resting on its hook
    public static final BooleanProperty HOOKED = BooleanProperty.create("hooked");

    // Model pixels with the dial facing north
    public static final Vec3 JACK = new Vec3(8.0, 3.0, 11.0);
    public static final double DIAL_TOP = 1.5;
    // Button positions call key first
    public static final Vec3[] BUTTONS = {
            new Vec3(8.0, DIAL_TOP, 6.0),
            new Vec3(8.0, DIAL_TOP, 7.5), new Vec3(6.75, DIAL_TOP, 7.25), new Vec3(6.5, DIAL_TOP, 6.0), new Vec3(6.75, DIAL_TOP, 4.75),
            new Vec3(8.0, DIAL_TOP, 4.5), new Vec3(9.25, DIAL_TOP, 4.75), new Vec3(9.5, DIAL_TOP, 6.0), new Vec3(9.25, DIAL_TOP, 7.25)
    };
    public static final int CALL = 0;
    private static final double BUTTON_REACH = 0.7;
    private static final float[][] BOXES = {
            {4.0F, 0.0F, 3.5F, 12.0F, 1.0F, 12.5F},
            {6.0F, 1.0F, 4.0F, 10.0F, 1.5F, 8.0F},
            {7.0F, 1.0F, 8.5F, 9.0F, 8.0F, 10.5F},
            {7.5F, 2.5F, 10.0F, 8.5F, 3.5F, 11.0F},
            {6.5F, 7.6F, 5.0F, 9.5F, 12.6F, 10.0F},
            {9.0F, 7.25F, 9.0F, 10.5F, 7.75F, 10.0F}
    };
    private static final float[] EARPIECE = {9.25F, 5.0F, 8.75F, 10.75F, 8.0F, 10.25F};
    private static final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();

    public TelephoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HOOKED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HOOKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.computeIfAbsent(state, TelephoneBlock::shape);
    }

    private static VoxelShape shape(BlockState state) {
        Direction facing = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();
        for (float[] box : BOXES) {
            shape = Shapes.or(shape, box(facing, box));
        }
        if (state.getValue(HOOKED)) {
            shape = Shapes.or(shape, box(facing, EARPIECE));
        }
        return shape.optimize();
    }

    private static VoxelShape box(Direction facing, float[] box) {
        Vec3 a = HfTransceiverPanel.toBlock(facing, box[0], box[1], box[2]);
        Vec3 b = HfTransceiverPanel.toBlock(facing, box[3], box[4], box[5]);
        return Shapes.box(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z), Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }

    // Button hit test
    public static int buttonAt(BlockState state, BlockPos pos, Vec3 hit) {
        Vec3 model = HfTransceiverPanel.toModel(state.getValue(FACING), pos, hit);
        if (Math.abs(model.y - DIAL_TOP) > 0.4) {
            return -1;
        }
        int nearest = -1;
        double closest = BUTTON_REACH;
        for (int i = 0; i < BUTTONS.length; i++) {
            double distance = Math.hypot(model.x - BUTTONS[i].x, model.z - BUTTONS[i].z);
            if (distance < closest) {
                closest = distance;
                nearest = i;
            }
        }
        return nearest;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof CableItem || AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (buttonAt(state, pos, hit.getLocation()) < 0 && stack.getItem() instanceof TelephoneEarpieceItem
                && level.getBlockEntity(pos) instanceof TelephoneBlockEntity phone) {
            return phone.useEarpiece(player, hand, stack) ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.FAIL;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TelephoneBlockEntity phone)) {
            return InteractionResult.PASS;
        }
        int button = buttonAt(state, pos, hit.getLocation());
        if (button < 0 && !state.getValue(HOOKED)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer server) {
            if (button >= 0) {
                phone.press(button, server);
            } else {
                phone.lift(server);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            if (level.getBlockEntity(pos) instanceof TelephoneBlockEntity phone) {
                Telephones.removed(server, phone, state.getValue(HOOKED));
            }
            Cables.broken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<TelephoneBlockEntity> getBlockEntityClass() {
        return TelephoneBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TelephoneBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.TELEPHONE.get();
    }
}
