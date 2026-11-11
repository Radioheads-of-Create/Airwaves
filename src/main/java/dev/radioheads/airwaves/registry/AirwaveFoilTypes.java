package dev.radioheads.airwaves.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.content.item.FoilItem;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.api.registry.foil.FoilTypeEntry;
import dev.radioheads.airwaves.api.util.ColorUtil;

public final class AirwaveFoilTypes {
    private AirwaveFoilTypes() {}

    public static final FoilTypeEntry
            COPPER = register("copper", AirwaveItems.COPPER_FOIL, ColorUtil.rgb(184, 115, 51)),
            GOLD = register("gold", AirwaveItems.GOLD_FOIL, ColorUtil.rgb(255, 215, 0)),
            SILVER = register("silver", AirwaveItems.SILVER_FOIL, ColorUtil.rgb(192, 192, 192));

    private static <T extends FoilItem> FoilTypeEntry register(String name, ItemEntry<T> foil, int color) {
        return Airwaves.REGISTRATE.foilType(name, () -> new FoilType(foil, color)).register();
    }

    public static void init() {}
}
