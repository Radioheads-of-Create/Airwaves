package dev.radioheads.airwaves.content.block;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.entity.block.RadioBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadioBlock extends HorizontalKineticBlock implements IBE<RadioBlockEntity> {
    public RadioBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<RadioBlockEntity> getBlockEntityClass() {
        return RadioBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RadioBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.RADIO.get();
    }
}
