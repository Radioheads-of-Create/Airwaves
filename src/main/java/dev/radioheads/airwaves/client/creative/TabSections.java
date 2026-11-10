package dev.radioheads.airwaves.client.creative;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.registry.AirwaveCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Creative tab sections under banner rows
@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public class TabSections {
    private static final int COLS = 9;
    private static final int ROWS = 5;
    private static final int TITLE_BG = 0xB0303030;
    private static final Map<Integer, String> HEADERS = new HashMap<>();

    private static boolean ours() {
        return CreativeModeInventoryScreen.selectedTab == AirwaveCreativeTabs.MAIN.get();
    }

    private static void pad(List<ItemStack> out) {
        while (out.size() % COLS != 0) {
            out.add(ItemStack.EMPTY);
        }
    }

    @SubscribeEvent
    public static void layout(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen) || !ours()) {
            return;
        }
        NonNullList<ItemStack> items = screen.getMenu().items;
        // Runs once per tab opening
        if (items.isEmpty() || items.getFirst().isEmpty()) {
            return;
        }

        List<AirwaveCreativeTabs.Section> sections = AirwaveCreativeTabs.sections();
        Map<Item, int[]> where = new HashMap<>();
        List<List<List<ItemStack>>> grouped = new ArrayList<>();
        for (int s = 0; s < sections.size(); s++) {
            List<List<ItemStack>> blocks = new ArrayList<>();
            for (int b = 0; b < sections.get(s).blocks().size(); b++) {
                for (ItemLike item : sections.get(s).blocks().get(b)) {
                    where.put(item.asItem(), new int[]{s, b});
                }
                blocks.add(new ArrayList<>());
            }
            grouped.add(blocks);
        }

        List<ItemStack> rest = new ArrayList<>();
        for (ItemStack stack : items) {
            int[] at = where.get(stack.getItem());
            if (at == null) {
                rest.add(stack);
            } else {
                grouped.get(at[0]).get(at[1]).add(stack);
            }
        }

        List<ItemStack> out = new ArrayList<>();
        HEADERS.clear();
        for (int s = 0; s < sections.size(); s++) {
            if (grouped.get(s).stream().allMatch(List::isEmpty)) {
                continue;
            }
            pad(out);
            HEADERS.put(out.size() / COLS, sections.get(s).id());
            for (int i = 0; i < COLS; i++) {
                out.add(ItemStack.EMPTY);
            }
            for (List<ItemStack> block : grouped.get(s)) {
                if (!block.isEmpty()) {
                    out.addAll(block);
                    pad(out);
                }
            }
        }
        out.addAll(rest);
        items.clear();
        items.addAll(out);
        screen.getMenu().scrollTo(screen.scrollOffs);
    }

    @SubscribeEvent
    public static void banners(ContainerScreenEvent.Render.Foreground event) {
        if (!(event.getContainerScreen() instanceof CreativeModeInventoryScreen screen) || !ours() || HEADERS.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> items = screen.getMenu().items;
        if (items.isEmpty() || !items.getFirst().isEmpty()) {
            return;
        }

        int top = screen.getMenu().getRowIndexForScroll(screen.scrollOffs);
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        for (int r = 0; r < ROWS; r++) {
            String id = HEADERS.get(top + r);
            if (id == null) {
                continue;
            }
            int x = 8, y = 17 + r * 18;
            graphics.blitSprite(Airwaves.asResource("section/" + id), x, y, COLS * 18, 18);
            Component title = Component.translatable("airwaves.section." + id);
            graphics.fill(x + 2, y + 2, x + font.width(title) + 9, y + 16, TITLE_BG);
            graphics.drawString(font, title, x + 6, y + 5, 0xFFFFFF, true);
        }
    }
}
