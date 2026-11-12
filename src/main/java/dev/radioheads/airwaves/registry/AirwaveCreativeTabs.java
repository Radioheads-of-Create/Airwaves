package dev.radioheads.airwaves.registry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class AirwaveCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Airwaves.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
            .withTabsBefore(AllCreativeModeTabs.BASE_CREATIVE_TAB.getId())
            .title(Component.translatable("creative_tab.airwave_main"))
            .icon(AirwaveBlocks.RADIO::asStack)
            .build());

    // Section with item rows
    public record Section(String id, List<List<ItemLike>> blocks) {}

    public static List<Section> sections() {
        return List.of(
                new Section("hardware", List.of(
                        List.of(AirwaveItems.FIELD_MANUAL, AirwaveBlocks.TUNING_TABLE, AirwaveBlocks.RADIO, AirwaveItems.HANDHELD_RADIO),
                        List.of(AirwaveBlocks.AM_TRANSMITTER, AirwaveBlocks.FM_TRANSMITTER, AirwaveBlocks.HF_TRANSCEIVER, AirwaveBlocks.VHF_BASE_STATION,
                                AirwaveBlocks.UHF_BASE_STATION, AirwaveBlocks.VHF_REPEATER, AirwaveBlocks.UHF_REPEATER, AirwaveBlocks.AMPLIFIER,
                                AirwaveBlocks.MICROPHONE),
                        List.of(AirwaveBlocks.COPPER_ANTENNA_ROD, AirwaveBlocks.GOLD_ANTENNA_ROD, AirwaveBlocks.SILVER_ANTENNA_ROD,
                                AirwaveBlocks.COPPER_ROD_BASE, AirwaveBlocks.GOLD_ROD_BASE, AirwaveBlocks.SILVER_ROD_BASE,
                                AirwaveBlocks.ANDESITE_INSULATOR, AirwaveBlocks.BRASS_INSULATOR))),
                new Section("materials", List.of(
                        List.of(AirwaveBlocks.SILVER_ORE, AirwaveItems.RAW_SILVER, AirwaveItems.SILVER_INGOT, AirwaveItems.SILVER_NUGGET,
                                AirwaveBlocks.SILVER_BLOCK, AirwaveItems.SILVER_SHEET, AirwaveItems.COPPER_WIRE, AirwaveItems.GOLD_WIRE, AirwaveItems.SILVER_WIRE),
                        List.of(AirwaveItems.COPPER_SPOOL, AirwaveItems.GOLD_SPOOL, AirwaveItems.SILVER_SPOOL,
                                AirwaveItems.COPPER_FOIL, AirwaveItems.GOLD_FOIL, AirwaveItems.SILVER_FOIL,
                                AirwaveItems.COPPER_CABLE_SPOOL, AirwaveItems.GOLD_CABLE_SPOOL, AirwaveItems.SILVER_CABLE_SPOOL),
                        List.of(AirwaveItems.EMPTY_SPOOL, AirwaveBlocks.SILVER_CASING, AirwaveItems.CUT_AMETHYST))),
                new Section("circuitry", List.of(
                        List.of(AirwaveItems.RESISTOR, AirwaveItems.SMALL_CAPACITOR, AirwaveItems.LARGE_CAPACITOR, AirwaveItems.TRANSISTOR,
                                AirwaveItems.LED, AirwaveItems.CRYSTAL_OSCILLATOR, AirwaveItems.MEMORY_CHIP, AirwaveItems.SPEAKER_CONE))),
                new Section("creative", List.of(
                        List.of(AirwaveItems.CREATIVE_WIRE, AirwaveItems.CREATIVE_SPOOL, AirwaveItems.CREATIVE_FOIL, AirwaveItems.CREATIVE_CABLE_SPOOL)
        ))
        );
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MAIN.get()) {
            Set<Item> sorted = new HashSet<>();
            for (Section section : sections()) {
                for (List<ItemLike> block : section.blocks()) {
                    for (ItemLike item : block) {
                        event.accept(item, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                        if (item == AirwaveBlocks.HF_TRANSCEIVER) {
                            event.accept(HfTransceiverBlock.withFinish(AirwaveBlocks.HF_TRANSCEIVER.asStack(), HfTransceiverBlock.Finish.BLACK),
                                    CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                        }
                        sorted.add(item.asItem());
                    }
                }
            }

            // Unsectioned items last
            for (RegistryEntry<Item, ?> item : Airwaves.REGISTRATE.getAll(Registries.ITEM)) {
                if (item.get() instanceof SequencedAssemblyItem || sorted.contains(item.get())) {
                    continue;
                }

                event.accept(item.get(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            }
        }
    }
}
