package dev.radioheads.airwaves.internal;

import dev.radioheads.airwaves.Airwaves;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class AirwaveEventListeners {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.sendSystemMessage(Component.literal("Airwaves is currently in alpha. Expect bugs and breaking changes on each update. Nothing is final.").withStyle(ChatFormatting.RED));
    }
}
