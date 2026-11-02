package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.world.item.ItemStack;

// Skin-effect metal loss per block per root MHz
public enum CableType {
    COPPER(0.00505),
    GOLD(0.00611),
    SILVER(0.00491);

    private final double lossPerBlock;

    CableType(double lossPerBlock) {
        this.lossPerBlock = lossPerBlock;
    }

    public double lossPerBlock() {
        return this.lossPerBlock;
    }

    public ItemStack cable(int count) {
        return switch (this) {
            case COPPER -> AirwaveItems.COPPER_CABLE_SPOOL.asStack(count);
            case GOLD -> AirwaveItems.GOLD_CABLE_SPOOL.asStack(count);
            case SILVER -> AirwaveItems.SILVER_CABLE_SPOOL.asStack(count);
        };
    }
}
