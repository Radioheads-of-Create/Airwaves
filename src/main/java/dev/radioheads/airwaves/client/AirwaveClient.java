package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.client.hf.HfTransceiverHud;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.content.screen.TuningTableScreen;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveItems;
import dev.radioheads.airwaves.registry.AirwaveMenuTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public class AirwaveClient {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AirwaveMenuTypes.TUNING_TABLE.get(), TuningTableScreen::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((itemStack, i) -> {
            if (i == 0) {
                DyedItemColor dyedColor = itemStack.get(DataComponents.DYED_COLOR);
                return dyedColor != null ? (0xFF000000 | dyedColor.rgb()) : 0xFFFFFFFF;
            }
            return 0xFFFFFFFF;
        }, AirwaveItems.LED.get());
    }

    // HF transceiver hints above the action bar
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.OVERLAY_MESSAGE, HfTransceiverHud.LAYER, HfTransceiverHud::render);
    }

    // HF transceiver item finish
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(AirwaveBlocks.HF_TRANSCEIVER.asItem(), Airwaves.asResource("finish"), (stack, level, entity, seed) -> {
            BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
            return properties != null && properties.get(HfTransceiverBlock.FINISH) == HfTransceiverBlock.Finish.BLACK ? 1.0F : 0.0F;
        }));
    }
}
