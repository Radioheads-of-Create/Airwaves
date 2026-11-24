package dev.radioheads.airwaves.internal;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.integration.IntegratedMods;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class AirwaveEventListeners {
    private static final int CABLE_STRETCH_CHECK_INTERVAL = 10;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.sendSystemMessage(Component.literal("Airwaves is currently in alpha. Expect bugs and breaking changes on each update. Nothing is final.").withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        FoilApplicatorBlockEntity.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!IntegratedMods.SABLE.isLoaded() || event.getServer().getTickCount() % CABLE_STRETCH_CHECK_INTERVAL != 0) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Cables.checkSublevelLinks(level);
        }
    }
}
