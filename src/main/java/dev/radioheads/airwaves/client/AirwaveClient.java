package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.screen.TuningTableScreen;
import dev.radioheads.airwaves.registry.AirwaveItems;
import dev.radioheads.airwaves.registry.AirwaveMenuTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

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
}
