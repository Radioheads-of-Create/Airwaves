package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.content.item.FoilItem;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class FoilApplicatorBlock extends BaseEntityBlock implements IBE<FoilApplicatorBlockEntity> {
    public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<FoilApplicatorBlock> CODEC = simpleCodec(FoilApplicatorBlock::new);

    public FoilApplicatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    public static Vec3 rollOffset(Direction facing) {
        return switch (facing) {
            case NORTH -> new Vec3(0, 10 / 16.0 - 0.7, -7 / 16.0);
            case SOUTH -> new Vec3(0, 10 / 16.0 - 0.7, 7 / 16.0);
            case WEST -> new Vec3(-7 / 16.0, 10 / 16.0 - 0.7, 0);
            case EAST -> new Vec3(7 / 16.0, 10 / 16.0 - 0.7, 0);
            default -> Vec3.ZERO;
        };
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof FoilItem
                && level.getBlockEntity(pos) instanceof FoilApplicatorBlockEntity applicator
                && applicator.getFoilInventory().isEmpty()) {
            applicator.getFoilInventory().insertItem(0, stack.copyWithCount(1), false);
            stack.shrink(1);
            applicator.notifyUpdate();
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()
                && level.getBlockEntity(pos) instanceof FoilApplicatorBlockEntity applicator
                && !applicator.getFoilInventory().isEmpty()) {
            ItemStack removed = applicator.getFoilInventory().extractItem(0, 1, false);
            player.getInventory().add(removed);
            applicator.notifyUpdate();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public Class<FoilApplicatorBlockEntity> getBlockEntityClass() {
        return FoilApplicatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FoilApplicatorBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.FOIL_APPLICATOR.get();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}