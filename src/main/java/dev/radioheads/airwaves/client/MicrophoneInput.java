package dev.radioheads.airwaves.client;

import com.simibubi.create.AllItems;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.content.block.MicrophoneBlock;
import dev.radioheads.airwaves.network.payload.MicrophonePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

// Tap to switch the microphone and hold to talk
@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class MicrophoneInput {
    @Nullable
    private static BlockPos pressing;

    private MicrophoneInput() {}

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        BlockPos target = pressing == null ? target(minecraft) : pressing;
        if (target == null) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (pressing == null) {
            pressing = target;
            PacketDistributor.sendToServer(new MicrophonePayload(target, MicrophonePayload.Action.PRESS));
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (pressing == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !minecraft.options.keyUse.isDown() || !pressing.equals(target(minecraft))) {
            if (minecraft.getConnection() != null) {
                PacketDistributor.sendToServer(new MicrophonePayload(pressing, MicrophonePayload.Action.RELEASE));
            }
            pressing = null;
        }
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockPos target = target(minecraft);
        int delta = (int) Math.signum(event.getScrollDeltaY());
        if (target == null || delta == 0 || !(minecraft.hitResult instanceof BlockHitResult hit)
                || !MicrophoneBlock.onKnob(minecraft.level.getBlockState(target), target, hit.getLocation())) {
            return;
        }
        event.setCanceled(true);
        PacketDistributor.sendToServer(new MicrophonePayload(target, delta > 0 ? MicrophonePayload.Action.TURN_UP : MicrophonePayload.Action.TURN_DOWN));
    }

    // Microphone under the crosshair unless a cable or wrench is in hand
    @Nullable
    private static BlockPos target(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.screen != null || player.isSpectator() || player.isShiftKeyDown()) {
            return null;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        ItemStack held = player.getMainHandItem();
        if (!(state.getBlock() instanceof MicrophoneBlock) || held.getItem() instanceof CableItem || AllItems.WRENCH.isIn(held)) {
            return null;
        }
        return hit.getBlockPos();
    }
}
