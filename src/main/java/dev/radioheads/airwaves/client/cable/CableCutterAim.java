package dev.radioheads.airwaves.client.cable;

import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableAim;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.content.item.CableCutterItem;
import dev.radioheads.airwaves.network.payload.CableCutPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Cable Cutter aim preview
@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class CableCutterAim {
    @Nullable
    private static CableAim.Target target;
    private static boolean useHeld;

    private CableCutterAim() {}

    @Nullable
    public static CableAim.Target target() {
        return target;
    }

    // After the Create overlay tick
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        useHeld = minecraft.options.keyUse.isDown();
        target = null;

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.isPaused() || player.isSpectator() || !CableCutterItem.holding(player)) {
            return;
        }

        Vec3 eye = player.getEyePosition();
        Optional<CableAim.Target> aimed = CableAim.aimed(level, eye, player.getViewVector(1.0F), player.blockInteractionRange());
        if (aimed.isEmpty() || hidden(minecraft.hitResult, aimed.get(), eye)) {
            return;
        }

        target = aimed.get();
        BlueprintOverlayRenderer.displayChainRequirements(target.type().spool().get(), target.spools(), true);
        player.displayClientMessage(CableItem.hint("length", target.length()), true);
        CablePreview.trail(level, target.fromAnchor(), target.toAnchor(), CablePreview.REFUSED_COLOR);
    }

    // Occlusion by blocks and entities
    private static boolean hidden(@Nullable HitResult hit, CableAim.Target aimed, Vec3 eye) {
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return false;
        }
        if (hit instanceof BlockHitResult block && (block.getBlockPos().equals(aimed.from()) || block.getBlockPos().equals(aimed.to()))) {
            return false;
        }
        return hit.getLocation().distanceTo(eye) < aimed.distance();
    }

    // Cut on use
    @SubscribeEvent
    public static void cut(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!event.isUseItem() || target == null || player == null || !(player.getItemInHand(event.getHand()).getItem() instanceof CableCutterItem)) {
            return;
        }
        event.setCanceled(true);
        if (useHeld) {
            event.setSwingHand(false);
            return;
        }
        PacketDistributor.sendToServer(new CableCutPayload(target.from(), target.to()));
        target = null;
    }
}
