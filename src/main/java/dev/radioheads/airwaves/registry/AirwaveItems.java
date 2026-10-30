package dev.radioheads.airwaves.registry;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.content.item.*;
import dev.radioheads.airwaves.api.data.CapacitorData;
import dev.radioheads.airwaves.content.item.EmptySpoolItem;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.data.AirwaveTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;


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
            LED = registerBoardItem("led", "LED"),
            TRANSISTOR = registerBoardItem("transistor");

    public static final ItemEntry<SequencedAssemblyItem>
            INCOMPLETE_RESISTOR = registerSequencedItem("incomplete_resistor"),
            INCOMPLETE_SPEAKER_CONE = registerSequencedItem("incomplete_speaker_cone");

    public static final ItemEntry<Item>
            RAW_SILVER = Airwaves.REGISTRATE.item("raw_silver", Item::new)
                .tag(Tags.Items.RAW_MATERIALS)
                .tag(AirwaveMaterials.SILVER.rawMaterial)
                .register(),
            SILVER_INGOT = Airwaves.REGISTRATE.item("silver_ingot", Item::new)
                    .tag(Tags.Items.INGOTS)
                    .tag(AirwaveMaterials.SILVER.ingots)
                    .register(),
            SILVER_NUGGET = Airwaves.REGISTRATE.item("silver_nugget", Item::new)
                    .tag(Tags.Items.NUGGETS)
                    .tag(AirwaveMaterials.SILVER.nuggets)
                    .register(),
            SILVER_SHEET = Airwaves.REGISTRATE.item("silver_sheet", Item::new)
                    .tag(AirwaveMaterials.SILVER.plates)
                    .register()
    ;


    public static final ItemEntry<SpoolItem>
        COPPER_SPOOL = registerSpool("copper"),
        GOLD_SPOOL = registerSpool("gold"),
        SILVER_SPOOL = registerSpool("silver");

    public static final ItemEntry<EmptySpoolItem>
            EMPTY_SPOOL = registerEmptySpool("empty");

    public static final ItemEntry<WireItem>
        COPPER_WIRE = registerWire("copper", AirwaveMaterials.COPPER),
        GOLD_WIRE = registerWire("gold", AirwaveMaterials.GOLD),
        SILVER_WIRE = registerWire("silver", AirwaveMaterials.SILVER);

    public static final ItemEntry<CapacitorItem>
        SMALL_CAPACITOR = registerCapacitor("small", 25),
        LARGE_CAPACITOR = registerCapacitor("large", 50);

    public static final ItemEntry<FoilItem>
        COPPER_FOIL = registerFoil("copper", AirwaveMaterials.COPPER),
        GOLD_FOIL = registerFoil("gold", AirwaveMaterials.GOLD),
        SILVER_FOIL = registerFoil("silver", AirwaveMaterials.SILVER);

    private static ItemEntry<SpoolItem> registerSpool(String path) {
        return Airwaves.REGISTRATE.item(path + "_spool", SpoolItem::new)
                .properties(p -> p.component(AirwaveDataComponents.SPOOL_AMOUNT, 100))
                .register();
    }

    private static ItemEntry<EmptySpoolItem> registerEmptySpool(String path) {
        return Airwaves.REGISTRATE.item(path + "_spool", EmptySpoolItem::new)
                .properties(p -> p.component(AirwaveDataComponents.SPOOL_AMOUNT, 100))
                .register();
    }

    private static ItemEntry<WireItem> registerWire(String path, AirwaveMaterials material) {
        return Airwaves.REGISTRATE.item(path + "_wire", WireItem::new)
                .tag(material.wire)
                .tag(AirwaveTags.Items.WIRES.tag)
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

    private static ItemEntry<FoilItem> registerFoil(String path, AirwaveMaterials material) {
        return Airwaves.REGISTRATE.item(path + "_foil", FoilItem::new)
                .tag(material.foil)
                .tag(AirwaveTags.Items.FOILS.tag)
                .register();
    }

    private static ItemEntry<BoardItem> registerBoardItem(String path) {
        return Airwaves.REGISTRATE.item(path, BoardItem::new).register();
    }

    private static ItemEntry<BoardItem> registerBoardItem(String path, String langOverride) {
        return Airwaves.REGISTRATE.item(path, BoardItem::new).lang(langOverride).register();
    }

    private static ItemEntry<SequencedAssemblyItem> registerSequencedItem(String path) {
        return Airwaves.REGISTRATE.item(path, SequencedAssemblyItem::new)
                .tag(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS)
                .register();
    }

    private static <T extends Item> ItemEntry<T> registerSimpleItem(String path, NonNullFunction<Item.Properties, T> item) {
        return Airwaves.REGISTRATE.item(path, item).register();
    }

    public static void init() {}
}
