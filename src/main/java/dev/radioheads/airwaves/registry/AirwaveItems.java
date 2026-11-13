package dev.radioheads.airwaves.registry;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.content.item.*;
import dev.radioheads.airwaves.api.data.CapacitorData;
import dev.radioheads.airwaves.content.item.CableCutterItem;
import dev.radioheads.airwaves.content.item.EmptySpoolItem;
import dev.radioheads.airwaves.content.item.FieldManualItem;
import dev.radioheads.airwaves.content.item.TelephoneEarpieceItem;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.data.AirwaveTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;

import java.util.function.Supplier;


public final class AirwaveItems {
    private AirwaveItems() {}

    public static final ItemEntry<Item>
            CUT_AMETHYST = Airwaves.REGISTRATE.item("cut_amethyst", Item::new)
                .tag(Tags.Items.GEMS)
                .tag(Tags.Items.GEMS_AMETHYST)
                .register();

    public static final ItemEntry<FieldManualItem>
            FIELD_MANUAL = Airwaves.REGISTRATE.item("field_manual", FieldManualItem::new)
                .properties(p -> p.stacksTo(1))
                .lang("Airwaves Field Manual")
                .register();

    public static final ItemEntry<Item>
            HANDHELD_RADIO = Airwaves.REGISTRATE.item("handheld_radio", Item::new)
                .properties(p -> p.stacksTo(1))
                .model(NonNullBiConsumer.noop())
                .register();

    public static final ItemEntry<TelephoneEarpieceItem>
            TELEPHONE_EARPIECE = Airwaves.REGISTRATE.item("telephone_earpiece", TelephoneEarpieceItem::new)
                .model((ctx, prov) -> prov.getBuilder(ctx.getName()).parent(new ModelFile.UncheckedModelFile(prov.modLoc("block/telephone/earpiece_item"))))
                .register();

    public static final ItemEntry<CableCutterItem>
            CABLE_CUTTER = Airwaves.REGISTRATE.item("cable_cutter", CableCutterItem::new)
                .properties(p -> p.stacksTo(1))
                .model((ctx, prov) -> prov.handheld(ctx))
                .register();

    public static final ItemEntry<BoardItem>
            CRYSTAL_OSCILLATOR = registerBoardItem("crystal_oscillator"),
            RESISTOR = registerBoardItem("resistor"),
            MEMORY_CHIP = registerBoardItem("memory_chip"),
            SPEAKER_CONE = registerBoardItem("speaker_cone"),
            TRANSISTOR = registerBoardItem("transistor"),
            LED = Airwaves.REGISTRATE.item("led", BoardItem::new)
                    .tag(ItemTags.DYEABLE)
                    .lang("LED")
                    // Dyed base with a plain overlay
                    .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/led"), prov.modLoc("item/led_overlay")))
                    .properties(properties -> properties
                            .component(DataComponents.DYED_COLOR,
                                    new DyedItemColor(0xFFFFFFFF, true)))
                    .register();

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
        SILVER_SPOOL = registerSpool("silver"),
        CREATIVE_SPOOL = registerSpool("creative");

    public static final ItemEntry<EmptySpoolItem>
            EMPTY_SPOOL = registerEmptySpool("empty");

    public static final ItemEntry<Item>
        COPPER_WIRE = registerWire("copper", AirwaveMaterials.COPPER),
        GOLD_WIRE = registerWire("gold", AirwaveMaterials.GOLD),
        SILVER_WIRE = registerWire("silver", AirwaveMaterials.SILVER),
        CREATIVE_WIRE = registerWire("creative", AirwaveMaterials.CREATIVE);

    public static final ItemEntry<CableItem>
        COPPER_CABLE_SPOOL = registerCable("copper", AirwaveMaterials.COPPER, AirwaveCableTypes.COPPER),
        GOLD_CABLE_SPOOL = registerCable("gold", AirwaveMaterials.GOLD, AirwaveCableTypes.GOLD),
        SILVER_CABLE_SPOOL = registerCable("silver", AirwaveMaterials.SILVER, AirwaveCableTypes.SILVER),
        CREATIVE_CABLE_SPOOL = registerCable("creative", AirwaveMaterials.CREATIVE, AirwaveCableTypes.CREATIVE);

    public static final ItemEntry<CapacitorItem>
        SMALL_CAPACITOR = registerCapacitor("small", 25),
        LARGE_CAPACITOR = registerCapacitor("large", 50);

    public static final ItemEntry<FoilItem>
        COPPER_FOIL = registerFoil("copper", AirwaveMaterials.COPPER),
        GOLD_FOIL = registerFoil("gold", AirwaveMaterials.GOLD),
        SILVER_FOIL = registerFoil("silver", AirwaveMaterials.SILVER),
        CREATIVE_FOIL = registerFoil("creative", AirwaveMaterials.CREATIVE);

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

    private static ItemEntry<Item> registerWire(String path, AirwaveMaterials material) {
        return Airwaves.REGISTRATE.item(path + "_wire", Item::new)
                .tag(material.wire)
                .tag(AirwaveTags.Items.WIRES.tag)
                .register();
    }

    private static ItemEntry<CableItem> registerCable(String path, AirwaveMaterials material, Supplier<CableType> type) {
        return Airwaves.REGISTRATE.item(path + "_cable_spool", properties -> new CableItem(properties, type))
                .tag(material.cable)
                .tag(AirwaveTags.Items.CABLES.tag)
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
                .properties(p -> p.component(AirwaveDataComponents.FOIL_AMOUNT, 250))
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
