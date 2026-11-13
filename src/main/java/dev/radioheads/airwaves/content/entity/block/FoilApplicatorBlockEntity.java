package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.List;

public class FoilApplicatorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation, Clearable {
    private SmartInventory inventory;
    private SmartInventory outputInventory;
    private SmartInventory foilInventory;

    public FoilApplicatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
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
