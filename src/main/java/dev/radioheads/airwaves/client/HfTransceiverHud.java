package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.content.block.HfTransceiverControl;
import dev.radioheads.airwaves.content.block.HfTransceiverHints;
import dev.radioheads.airwaves.content.entity.block.HfTransceiverBlockEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class HfTransceiverHud {
    public static final ResourceLocation LAYER = Airwaves.asResource("hf_transceiver_hint");
    private static final int MESSAGE_TICKS = 40;
    private static final int FADE_TICKS = 10;
    private static final int OVERLAY_Y = 68;

    private static long ticks;
    @Nullable
    private static BlockPos watched;
    @Nullable
    private static HfTransceiverHints.Panel panel;
    @Nullable
    private static HfTransceiverControl messageControl;
    @Nullable
    private static Component message;
    private static long messageAt;
    @Nullable
    private static Component shown;
    private static boolean shownMessage;
    @Nullable
    private static Component faded;
    private static long changedAt;

    private HfTransceiverHud() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused()) {
            return;
        }
        ticks++;

        HfTransceiverBlockEntity transceiver = null;
        HfTransceiverControl control = null;
        if (minecraft.player != null && !minecraft.player.isSpectator() && minecraft.level != null
                && minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof HfTransceiverBlockEntity found
                && found.getBlockState().getBlock() instanceof HfTransceiverBlock) {
            transceiver = found;
            control = HfTransceiverBlock.controlAt(found.getBlockState(), hit.getBlockPos(), hit.getLocation());
        }
        watch(transceiver, control);

        boolean showMessage = control != null && message != null && control == messageControl && ticks - messageAt < MESSAGE_TICKS;
        Component target = control == null ? null : showMessage ? message : HfTransceiverHints.label(control);
        if (!Objects.equals(target, shown)) {
            boolean fade = target == null || (shownMessage && control == messageControl && !showMessage);
            faded = fade ? shown : null;
            shown = target;
            changedAt = ticks;
        }
        shownMessage = showMessage;
    }

    private static void watch(@Nullable HfTransceiverBlockEntity transceiver, @Nullable HfTransceiverControl control) {
        if (transceiver == null) {
            watched = null;
            panel = null;
            message = null;
            return;
        }
        HfTransceiverHints.Panel now = transceiver.panel();
        if (!transceiver.getBlockPos().equals(watched) || panel == null) {
            watched = transceiver.getBlockPos();
            panel = now;
            message = null;
            return;
        }
        if (now.equals(panel)) {
            return;
        }
        Component change = control == null ? null : HfTransceiverHints.change(control, panel, now);
        if (change != null) {
            message = change;
            messageControl = control;
            messageAt = ticks;
        }
        panel = now;
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if ((shown == null && faded == null) || minecraft.gui.overlayMessageTime > 0) {
            return;
        }
        float progress = faded == null ? 1.0F : Mth.clamp((ticks - changedAt + delta.getGameTimeDeltaPartialTick(false)) / FADE_TICKS, 0.0F, 1.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() - OVERLAY_Y, 0.0F);
        draw(graphics, minecraft, faded, 1.0F - progress);
        draw(graphics, minecraft, shown, progress);
        graphics.pose().popPose();
    }

    private static void draw(GuiGraphics graphics, Minecraft minecraft, @Nullable Component text, float alpha) {
        int opacity = Math.round(alpha * 255.0F);
        if (text == null || opacity <= 8) {
            return;
        }
        Font font = minecraft.font;
        int width = font.width(text);
        int left = -width / 2;
        int background = minecraft.options.getBackgroundColor(0.0F);
        if (background != 0) {
            graphics.fill(left - 2, -6, left + width + 2, 7, FastColor.ARGB32.multiply(background, 0xFFFFFF | opacity << 24));
        }
        graphics.drawString(font, text, left, -4, CableItem.HINT_COLOR | opacity << 24);
    }
}
