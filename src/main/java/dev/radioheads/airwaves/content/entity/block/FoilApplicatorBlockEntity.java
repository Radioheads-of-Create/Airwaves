package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import dev.radioheads.airwaves.api.content.item.FoilItem;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;

public class FoilApplicatorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation, Clearable {
    private final SmartInventory inventory;
    private final SmartInventory outputInventory;
    private final SmartInventory foilInventory;

    public FoilApplicatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.inventory = new SmartInventory(1, this);
        this.outputInventory = new SmartInventory(1, this);
        this.foilInventory = new SmartInventory(1, this, (i, item) -> item.getItem() instanceof FoilItem);
    }

    public SmartInventory getInventory() {
        return this.inventory;
    }

    public SmartInventory getOutputInventory() {
        return this.outputInventory;
    }

    public SmartInventory getFoilInventory() {
        return this.foilInventory;
    }

    public ItemStack getFoil() {
        return this.foilInventory.getStackInSlot(0);
    }

    public ItemStack getOutputItem() {
        return this.outputInventory.getItem(0);
    }

    @Nullable
    public FoilType getFoilType() {
        return getFoil().getItem() instanceof FoilItem foil ? foil.getFoilType() : null;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        outputInventory.deserializeNBT(registries, compound.getCompound("OutputInventory"));
        foilInventory.deserializeNBT(registries, compound.getCompound("FoilInventory"));
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(registries));
        compound.put("OutputInventory", outputInventory.serializeNBT(registries));
        compound.put("FoilInventory", foilInventory.serializeNBT(registries));
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AirwaveBlockEntities.FOIL_APPLICATOR.get(), (be, dir) -> {
            if (dir == null) return new CombinedInvWrapper(be.inventory, be.outputInventory, be.foilInventory);
            Direction f = be.getBlockState().getValue(FoilApplicatorBlock.HORIZONTAL_FACING);
            return dir == f ? be.outputInventory : dir == f.getOpposite() ? be.inventory : be.foilInventory;
        });
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
        outputInventory.clearContent();
        foilInventory.clearContent();
    }
}
