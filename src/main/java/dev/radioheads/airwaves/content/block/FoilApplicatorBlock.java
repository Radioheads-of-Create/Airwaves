package dev.radioheads.airwaves.content.block;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FoilApplicatorBlock extends HorizontalKineticBlock implements IBE<FoilApplicatorBlockEntity> {
    public FoilApplicatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
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
