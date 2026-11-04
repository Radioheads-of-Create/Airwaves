package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.api.content.item.CableItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

// Skin-effect metal loss per block per root MHz
public record CableType(Supplier<? extends CableItem> spool, double lossPerBlock) {
    @Contract("_ -> new")
    public @NotNull ItemStack cable(int count) {
        return new ItemStack(spool.get(), count);
    }
}
