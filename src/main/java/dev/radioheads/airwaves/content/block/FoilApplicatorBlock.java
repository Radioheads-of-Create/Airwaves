package dev.radioheads.airwaves.content.block;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@NothingNullByDefault
public class FoilApplicatorBlock extends HorizontalKineticBlock implements IBE<FoilApplicatorBlockEntity> {
    public FoilApplicatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof FoilItem
                && level.getBlockEntity(pos) instanceof FoilApplicatorBlockEntity applicator
                && applicator.getFoilInventory().isEmpty()
        ) {
            applicator.getFoilInventory().insertItem(0, stack.copyWithCount(1), false);
            stack.shrink(1);
            applicator.notifyUpdate();
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FoilApplicatorBlockEntity applicator && !applicator.getFoilInventory().isEmpty()) {
            ItemStack removed = applicator.getFoilInventory().extractItem(0, 1, false);
            player.getInventory().add(removed);
            applicator.notifyUpdate();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    @Override
    public Class<FoilApplicatorBlockEntity> getBlockEntityClass() {
        return FoilApplicatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FoilApplicatorBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.FOIL_APPLICATOR.get();
    }
}
