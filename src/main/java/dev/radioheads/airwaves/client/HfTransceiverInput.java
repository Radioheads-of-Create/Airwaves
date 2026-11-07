package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.content.block.HfTransceiverControl;
import dev.radioheads.airwaves.network.payload.HfPanelPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class HfTransceiverInput {
    private HfTransceiverInput() {}

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null || minecraft.level == null || minecraft.player.isSpectator()) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof HfTransceiverBlock)) {
            return;
        }
        HfTransceiverControl control = HfTransceiverBlock.controlAt(state, hit.getBlockPos(), hit.getLocation());
        int delta = (int) Math.signum(event.getScrollDeltaY());
        if (control == null || control.kind == HfTransceiverControl.Kind.KEY || delta == 0) {
            return;
        }
        event.setCanceled(true);
        PacketDistributor.sendToServer(new HfPanelPayload(hit.getBlockPos(), control.ordinal(), delta));
    }
}
