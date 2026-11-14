package dev.radioheads.airwaves.client.cable;

import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.Optional;

@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public class CablePreview {
    public static final int REFUSED_COLOR = 0xFF5D5D;
    private static final double TRAIL_SPACING = 0.25;
    private static final float TRAIL_CHANCE = 0.1F;

    private static boolean useHeld;

    // Runs after Create's tick
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        useHeld = minecraft.options.keyUse.isDown();

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.isPaused()
                || !(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        ItemStack stack = selecting(player);
        BlockPos start = stack.get(AirwaveDataComponents.CABLE_START);
        BlockPos end = hit.getBlockPos();
        if (start == null || start.equals(end)) {
            return;
        }

        CableNode from = Cables.node(level, start);
        CableNode to = Cables.node(level, end);
        if (from == null || to == null) {
            return;
        }

        CableItem cable = (CableItem) stack.getItem();
        int length = Cables.length(start, end);
        int spools = Cables.spools(length);
        Optional<Component> refusal = cable.refusal(player, level, start, end);

        if (length <= Cables.MAX_LENGTH && !player.hasInfiniteMaterials()) {
            BlueprintOverlayRenderer.displayChainRequirements(cable, spools, cable.hasSpools(player, spools));
        }
        player.displayClientMessage(refusal.orElseGet(() -> CableItem.hint("length", length)), true);
        trail(level, from.anchor(), to.anchor(), refusal.isPresent() ? REFUSED_COLOR : CableItem.HINT_COLOR);
    }

    // Ignores held-button repeats
    @SubscribeEvent
    public static void stopHeldClicks(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (useHeld && event.isUseItem() && player != null && player.getItemInHand(event.getHand()).getItem() instanceof CableItem) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    private static ItemStack selecting(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof CableItem && stack.has(AirwaveDataComponents.CABLE_START)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void trail(ClientLevel level, Vec3 from, Vec3 to, int color) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        DustParticleOptions dust = new DustParticleOptions(Vec3.fromRGB24(color).toVector3f(), 1.0F);

        for (double distance = 0.0; distance <= length; distance += TRAIL_SPACING) {
            if (level.random.nextFloat() < TRAIL_CHANCE) {
                Vec3 point = CableRenderer.along(from, delta, distance / length);
                level.addParticle(dust, true, point.x, point.y, point.z, 0.0, 0.0, 0.0);
            }
        }
    }
}
