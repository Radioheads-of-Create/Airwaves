package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@NothingNullByDefault
public class RadioBlockEntity extends KineticBlockEntity {
    private Tuning tuning = Tuning.DEFAULT;

    public RadioBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(AirwaveDataComponents.TUNING, tuning);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.tuning.writeTo(compound, registries);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.tuning = Tuning.readFrom(compound, registries);
    }
}
