package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.api.content.item.CableItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

// Skin-effect metal loss per block per root MHz
public record CableType(Supplier<? extends CableItem> spool, double lossPerBlock, int color, double delayMsPerBlock) {
    // Copper 100 ms per 128 blocks
    public static final double COPPER_DELAY_MS_PER_BLOCK = 100.0 / 128.0;

    @Contract("_ -> new")
    public @NotNull ItemStack cable(int count) {
        return new ItemStack(spool.get(), count);
    }
}
