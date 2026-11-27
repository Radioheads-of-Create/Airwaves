package dev.radioheads.airwaves.integration.ponder.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.createmod.ponder.api.element.PonderOverlayElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class RadioWaveOverlay implements PonderOverlayElement {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "airwaves",
            "textures/gui/ponder/radio_waves.png"
    );

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final int FRAME_COUNT = 5;
    private static final int FRAME_DURATION = 2;

    private int ticks = 0;
    private boolean visible = true;

    @Override
    public void render(
            PonderScene scene,
            PonderUI screen,
            GuiGraphics graphics,
            float partialTicks
    ) {
        int frame = (ticks / FRAME_DURATION) % FRAME_COUNT;

        int x = graphics.guiWidth() / 2 - FRAME_WIDTH / 2;
        int y = graphics.guiHeight() / 2 - FRAME_HEIGHT / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.blit(
                TEXTURE,
                x,
                y,
                frame * FRAME_WIDTH,
                0,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                FRAME_WIDTH * FRAME_COUNT,
                FRAME_HEIGHT
        );

        RenderSystem.disableBlend();
    }

    @Override
    public void tick(PonderScene scene) {
        ticks++;
    }

    @Override
    public void reset(PonderScene scene) {
        ticks = 0;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
