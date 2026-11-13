package dev.radioheads.airwaves.client.telephone;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.content.block.TelephoneBlock;
import dev.radioheads.airwaves.content.entity.block.TelephoneBlockEntity;
import dev.radioheads.airwaves.content.telephone.PhoneNumbers;
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

// Dial hints
@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class TelephoneHud {
    public static final ResourceLocation LAYER = Airwaves.asResource("telephone_hint");
    private static final int NUMBER_TICKS = 40;
    private static final int FADE_TICKS = 10;
    private static final int OVERLAY_Y = 68;

    private static long ticks;
    @Nullable
    private static BlockPos watched;
    private static String dialed = "";
    private static long dialedAt = -NUMBER_TICKS;
    @Nullable
    private static Component shown;
    @Nullable
    private static Component faded;
    private static long changedAt;

    private TelephoneHud() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused()) {
            return;
        }
        ticks++;

        Component target = null;
        if (minecraft.player != null && !minecraft.player.isSpectator() && minecraft.level != null
                && minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof TelephoneBlockEntity phone
                && phone.getBlockState().getBlock() instanceof TelephoneBlock) {
            String now = phone.dialed();
            if (!phone.getBlockPos().equals(watched)) {
                watched = phone.getBlockPos();
                dialedAt = -NUMBER_TICKS;
            } else if (!now.equals(dialed)) {
                dialedAt = ticks;
            }
            dialed = now;
            int button = TelephoneBlock.buttonAt(phone.getBlockState(), hit.getBlockPos(), hit.getLocation());
            if (!now.isEmpty() && ticks - dialedAt < NUMBER_TICKS) {
                target = Component.literal(PhoneNumbers.format(now));
            } else if (button == TelephoneBlock.CALL) {
                target = Component.translatable("airwaves.telephone.confirm");
            } else if (button > 0) {
                target = Component.translatable("airwaves.telephone.dial", button);
            }
        } else {
            watched = null;
        }

        if (!Objects.equals(target, shown)) {
            faded = shown;
            shown = target;
            changedAt = ticks;
        }
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if ((shown == null && faded == null) || minecraft.gui.overlayMessageTime > 0) {
            return;
        }
        float progress = Mth.clamp((ticks - changedAt + delta.getGameTimeDeltaPartialTick(false)) / FADE_TICKS, 0.0F, 1.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() - OVERLAY_Y, 0.0F);
        draw(graphics, minecraft, faded, 1.0F - progress);
        draw(graphics, minecraft, shown, shown == null ? 0.0F : faded == null ? 1.0F : progress);
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
