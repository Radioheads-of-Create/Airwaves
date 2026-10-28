package dev.radioheads.airwaves.registry;

import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.content.item.*;
import dev.radioheads.airwaves.api.data.CapacitorData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

public final class AirwaveItems {
    private AirwaveItems() {}

    public static final ItemEntry<Item>
            CUT_AMETHYST = Airwaves.REGISTRATE.item("cut_amethyst", Item::new)
                .tag(Tags.Items.GEMS)
                .tag(Tags.Items.GEMS_AMETHYST)
                .register();

    public static final ItemEntry<BoardItem>
            CRYSTAL_OSCILLATOR = registerBoardItem("crystal_oscillator"),
            RESISTOR = registerBoardItem("resistor"),
            MEMORY_CHIP = registerBoardItem("memory_chip"),
            SPEAKER_CONE = registerBoardItem("speaker_cone"),
            TRANSISTOR = registerBoardItem("transistor");

    public static final ItemEntry<Item>
            RAW_SILVER = Airwaves.REGISTRATE.item("raw_silver", Item::new)
                .tag(Tags.Items.RAW_MATERIALS)
                .tag(AirwaveTags.Items.RAW_MATERIALS_SILVER.tag)
                .register(),
            SILVER_INGOT = Airwaves.REGISTRATE.item("silver_ingot", Item::new)
                    .tag(Tags.Items.INGOTS)
                    .tag(AirwaveTags.Items.INGOTS_SILVER.tag)
                    .register(),
            SILVER_NUGGET = Airwaves.REGISTRATE.item("silver_nugget", Item::new)
                    .tag(Tags.Items.NUGGETS)
                    .tag(AirwaveTags.Items.NUGGETS_SILVER.tag)
                    .register(),
            SILVER_SHEET = Airwaves.REGISTRATE.item("silver_sheet", Item::new)
                    .tag(CommonMetal.SILVER.plates)
                    .register()
    ;


    public static final ItemEntry<SpoolItem>
        EMPTY_COIL = registerSpool("empty"),
        COPPER_COIL = registerSpool("copper"),
        GOLD_COIL = registerSpool("gold"),
        SILVER_COIL = registerSpool("silver");

    public static final ItemEntry<WireItem>
        COPPER_WIRE = registerWire("copper"),
        GOLD_WIRE = registerWire("gold"),
        SILVER_WIRE = registerWire("silver");

    public static final ItemEntry<CapacitorItem>
        SMALL_CAPACITOR = registerCapacitor("small", 25),
        LARGE_CAPACITOR = registerCapacitor("large", 50);

    public static final ItemEntry<FoilItem>
        COPPER_FOIL = registerFoil("copper"),
        GOLD_FOIL = registerFoil("gold"),
        SILVER_FOIL = registerFoil("silver");

    private static ItemEntry<SpoolItem> registerSpool(String path) {
        return Airwaves.REGISTRATE.item(path + "_spool", SpoolItem::new)
                .properties(p -> p.component(AirwaveDataComponents.SPOOL_AMOUNT, 100))
                .register();
    }

    private static ItemEntry<WireItem> registerWire(String path) {
        return Airwaves.REGISTRATE.item(path + "_wire", WireItem::new)
                .register();
    }

    private static ItemEntry<CapacitorItem> registerCapacitor(String path, int maxCharge) {
        return Airwaves.REGISTRATE.item(path + "_capacitor", CapacitorItem::new)
                .properties(p -> p.component(AirwaveDataComponents.CAPACITOR_DATA, new CapacitorData(0, maxCharge)))
                .register();
    }

    private static ItemEntry<Item> registerSimpleItem(String path) {
        return Airwaves.REGISTRATE.item(path, Item::new).register();
    }

    @SafeVarargs
    private static ItemEntry<Item> registerSimpleItem(String path, TagKey<Item>... tag) {
        return Airwaves.REGISTRATE.item(path, Item::new).tag(tag).register();
    }

    private static ItemEntry<FoilItem> registerFoil(String path) {
        return Airwaves.REGISTRATE.item(path + "_foil", FoilItem::new).register();
    }

    private static ItemEntry<BoardItem> registerBoardItem(String path) {
        return Airwaves.REGISTRATE.item(path, BoardItem::new).register();
    }

    private static <T extends Item> ItemEntry<T> registerSimpleItem(String path, NonNullFunction<Item.Properties, T> item) {
        return Airwaves.REGISTRATE.item(path, item).register();
    }

    public static void init() {}
}
