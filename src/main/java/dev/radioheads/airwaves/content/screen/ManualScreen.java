package dev.radioheads.airwaves.content.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.radioheads.airwaves.Airwaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ManualScreen extends Screen {
    private static final ResourceLocation NEXT = ResourceLocation.withDefaultNamespace("widget/page_forward");
    private static final ResourceLocation NEXT_HOVER = ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted");
    private static final ResourceLocation PREV = ResourceLocation.withDefaultNamespace("widget/page_backward");
    private static final ResourceLocation PREV_HOVER = ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted");
    private static final int ARROW_W = 23;
    private static final int ARROW_H = 13;
    private static final int ARROW_Y = 231;
    private static final int PREV_X = 14;
    private static final int NEXT_X = 367;
    private static final ResourceLocation COVER = Airwaves.asResource("textures/gui/manual/cover.png");
    private static final int COVER_WIDTH = 400;
    private static final int PAPER_INSET = 6;
    private static final int SPINE_X = 199;
    private static final int SPINE_WIDTH = 2;
    private static final int TAB_X = 399;
    private static final int TAB_WIDTH = 7;
    private static final int TAB_OPEN_WIDTH = 10;
    private static final int TAB_HEIGHT = 30;
    private static final int TAB_EDGE = 0xFF1C140C;
    private static final int TAB_SHINE = 0x26FFFFFF;
    private static final int LINK_HOVER = 0xB3000000 | ManualBook.SHADE;

    private static int lastSpread;

    private ManualBook book;
    private int at;
    private float scale = 1.0F;
    private float left;
    private float top;

    public static void open() {
        Minecraft.getInstance().setScreen(new ManualScreen());
    }

    protected ManualScreen() {
        super(Component.translatable("item.airwaves.field_manual"));
    }

    @Override
    protected void init() {
        this.book = ManualBook.load(this.font);
        this.at = Math.clamp(lastSpread, 0, Math.max(0, this.book.spreads().size() - 1));
        this.scale = Math.min(1.0F, Math.min((this.width - 2) / (float) ManualBook.WIDTH, (this.height - 2) / (float) ManualBook.HEIGHT));
        this.left = (this.width - ManualBook.WIDTH * this.scale) / 2.0F;
        this.top = (this.height - ManualBook.HEIGHT * this.scale) / 2.0F;
        if (this.scale >= 1.0F) {
            this.left = Mth.floor(this.left);
            this.top = Mth.floor(this.top);
        }
    }

    private float bookX(double mouseX) {
        return (float) ((mouseX - this.left) / this.scale);
    }

    private float bookY(double mouseY) {
        return (float) ((mouseY - this.top) / this.scale);
    }

    private ManualBook.Spread spread() {
        return this.book.spreads().get(this.at);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.book.spreads().isEmpty()) {
            return;
        }

        float x = bookX(mouseX);
        float y = bookY(mouseY);
        graphics.pose().pushPose();
        graphics.pose().translate(this.left, this.top, 0.0F);
        graphics.pose().scale(this.scale, this.scale, 1.0F);

        graphics.fill(PAPER_INSET, PAPER_INSET, COVER_WIDTH - PAPER_INSET, ManualBook.HEIGHT - PAPER_INSET, 0xFF000000 | ManualBook.PAPER);
        graphics.fill(SPINE_X, PAPER_INSET, SPINE_X + SPINE_WIDTH, ManualBook.HEIGHT - PAPER_INSET, 0xFF000000 | ManualBook.SHADE);
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(COVER, 0, 0, 0, 0, COVER_WIDTH, ManualBook.HEIGHT, COVER_WIDTH, ManualBook.HEIGHT);

        ManualBook.Spread spread = spread();
        for (ManualBook.Link link : spread.links()) {
            if (x >= link.x() && x < link.x() + link.w() && y >= link.y() && y < link.y() + link.h()) {
                graphics.fill(link.x(), link.y(), link.x() + link.w(), link.y() + link.h(), LINK_HOVER);
            }
        }

        ItemStack hoveredIcon = ItemStack.EMPTY;
        for (ManualBook.Op op : spread.ops()) {
            switch (op) {
                case ManualBook.Rect rect -> graphics.fill(rect.x(), rect.y(), rect.x() + rect.w(), rect.y() + rect.h(), rect.argb());
                case ManualBook.Text text -> graphics.drawString(this.font, text.text(), text.x(), text.y(), 0xFF000000 | text.rgb(), false);
                case ManualBook.Dots dots -> {
                    for (int i = 0; i < dots.w(); i += 2) {
                        graphics.fill(dots.x() + i, dots.y(), dots.x() + i + 1, dots.y() + 1, 0xFF000000 | dots.rgb());
                    }
                }
                case ManualBook.Picture picture -> {
                    RenderSystem.enableBlend();
                    graphics.blit(picture.texture(), picture.x(), picture.y(), 0, 0, picture.w(), picture.h(), picture.w(), picture.h());
                }
                case ManualBook.Icon icon -> {
                    graphics.renderItem(icon.stack(), icon.x(), icon.y());
                    if (x >= icon.x() && x < icon.x() + 16 && y >= icon.y() && y < icon.y() + 16) {
                        hoveredIcon = icon.stack();
                    }
                }
            }
        }

        for (int i = 0; i < this.book.tabs().size(); i++) {
            ManualBook.Tab tab = this.book.tabs().get(i);
            int width = i == spread.group() ? TAB_OPEN_WIDTH : TAB_WIDTH;
            graphics.fill(TAB_X, tab.y(), TAB_X + width, tab.y() + TAB_HEIGHT, TAB_EDGE);
            graphics.fill(TAB_X, tab.y() + 1, TAB_X + width - 1, tab.y() + TAB_HEIGHT - 1, 0xFF000000 | tab.rgb());
            graphics.fill(TAB_X, tab.y() + 1, TAB_X + width - 1, tab.y() + 2, TAB_SHINE);
        }

        if (this.at > 0) {
            graphics.blitSprite(over(PREV_X, x, y) ? PREV_HOVER : PREV, PREV_X, ARROW_Y, ARROW_W, ARROW_H);
        }
        if (this.at < this.book.spreads().size() - 1) {
            graphics.blitSprite(over(NEXT_X, x, y) ? NEXT_HOVER : NEXT, NEXT_X, ARROW_Y, ARROW_W, ARROW_H);
        }
        graphics.pose().popPose();

        ManualBook.Tab hovered = tabAt(x, y);
        if (hovered != null) {
            graphics.renderTooltip(this.font, Component.literal(hovered.name()), mouseX, mouseY);
        } else if (!hoveredIcon.isEmpty()) {
            graphics.renderTooltip(this.font, hoveredIcon.getHoverName(), mouseX, mouseY);
        }
    }

    private boolean over(int arrowX, float x, float y) {
        return x >= arrowX && x < arrowX + ARROW_W && y >= ARROW_Y && y < ARROW_Y + ARROW_H;
    }

    @Nullable
    private ManualBook.Tab tabAt(float x, float y) {
        for (ManualBook.Tab tab : this.book.tabs()) {
            if (x >= TAB_X && x < TAB_X + TAB_OPEN_WIDTH && y >= tab.y() && y < tab.y() + TAB_HEIGHT) {
                return tab;
            }
        }
        return null;
    }

    private void go(int to) {
        int target = Math.clamp(to, 0, this.book.spreads().size() - 1);
        if (target != this.at) {
            this.at = target;
            lastSpread = target;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !this.book.spreads().isEmpty()) {
            float x = bookX(mouseX);
            float y = bookY(mouseY);
            if (this.at > 0 && over(PREV_X, x, y)) {
                go(this.at - 1);
                return true;
            }
            if (this.at < this.book.spreads().size() - 1 && over(NEXT_X, x, y)) {
                go(this.at + 1);
                return true;
            }

            for (ManualBook.Link link : spread().links()) {
                if (x >= link.x() && x < link.x() + link.w() && y >= link.y() && y < link.y() + link.h()) {
                    go(link.spread());
                    return true;
                }
            }

            ManualBook.Tab tab = tabAt(x, y);
            if (tab != null) {
                go(tab.spread());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0 && !this.book.spreads().isEmpty()) {
            go(this.at + (scrollY > 0.0 ? -1 : 1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.book.spreads().isEmpty()) {
            return super.keyPressed(key, scanCode, modifiers);
        }

        switch (key) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_PAGE_UP, GLFW.GLFW_KEY_A -> go(this.at - 1);
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_PAGE_DOWN, GLFW.GLFW_KEY_D -> go(this.at + 1);
            case GLFW.GLFW_KEY_HOME -> go(0);
            case GLFW.GLFW_KEY_END -> go(this.book.spreads().size() - 1);
            default -> {
                return super.keyPressed(key, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
