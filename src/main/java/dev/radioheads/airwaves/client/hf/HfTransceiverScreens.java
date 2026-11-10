package dev.radioheads.airwaves.client.hf;

import com.mojang.blaze3d.platform.NativeImage;
import dev.radioheads.airwaves.Airwaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class HfTransceiverScreens {
    private static final long IDLE_TICKS = 100L;
    private static final Map<Long, Screen> SCREENS = new HashMap<>();
    private static long clock;

    private static final class Screen {
        final DynamicTexture texture = new DynamicTexture(HfTransceiverDisplay.WIDTH, HfTransceiverDisplay.HEIGHT, false);
        final ResourceLocation location;
        final int[] pixels = new int[HfTransceiverDisplay.WIDTH * HfTransceiverDisplay.HEIGHT];
        HfTransceiverDisplay.Frame frame;
        long usedAt;

        Screen(BlockPos pos) {
            this.location = Airwaves.asResource("hf_transceiver_screen/" + Long.toHexString(pos.asLong()));
            Minecraft.getInstance().getTextureManager().register(this.location, this.texture);
        }

        void show(HfTransceiverDisplay.Frame frame) {
            if (frame.equals(this.frame)) {
                return;
            }
            this.frame = frame;
            HfTransceiverDisplay.paint(this.pixels, frame);
            NativeImage image = this.texture.getPixels();
            if (image == null) {
                return;
            }
            for (int y = 0; y < HfTransceiverDisplay.HEIGHT; y++) {
                for (int x = 0; x < HfTransceiverDisplay.WIDTH; x++) {
                    int argb = this.pixels[y * HfTransceiverDisplay.WIDTH + x];
                    int abgr = (argb & 0xFF00FF00) | (argb & 0xFF) << 16 | (argb >> 16) & 0xFF;
                    image.setPixelRGBA(x, y, abgr);
                }
            }
            this.texture.upload();
        }
    }

    private HfTransceiverScreens() {}

    public static ResourceLocation show(BlockPos pos, HfTransceiverDisplay.Frame frame) {
        Screen screen = SCREENS.computeIfAbsent(pos.asLong(), key -> new Screen(pos));
        screen.usedAt = clock;
        screen.show(frame);
        return screen.location;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        clock++;
        if (clock % 20 != 0) {
            return;
        }
        Iterator<Screen> screens = SCREENS.values().iterator();
        while (screens.hasNext()) {
            Screen screen = screens.next();
            if (clock - screen.usedAt > IDLE_TICKS) {
                Minecraft.getInstance().getTextureManager().release(screen.location);
                screens.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            SCREENS.values().forEach(screen -> Minecraft.getInstance().getTextureManager().release(screen.location));
            SCREENS.clear();
        }
    }
}
