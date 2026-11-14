package dev.radioheads.airwaves.api.content.item;

import dev.radioheads.airwaves.api.foil.FoilType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class FoilItem extends Item {
    private final Supplier<FoilType> type;

    public FoilItem(Properties properties, Supplier<FoilType> type) {
        super(properties);
        this.type = type;
    }

    public FoilType getFoilType() {
        return type.get();
    }
}
