package dev.radioheads.airwaves.content.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import dev.radioheads.airwaves.Airwaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class ManualBook {
    static final int WIDTH = 410;
    static final int HEIGHT = 252;
    static final int INK = 3877402;
    static final int INK2 = 7232068;
    static final int INK3 = 10521194;
    static final int RED = 9185820;
    static final int PAPER = 15721154;
    static final int SHADE = 14798758;

    private static final int LEFT_X = 22;
    private static final int RIGHT_X = 214;
    private static final int COLUMN = 164;
    private static final int LEFT_NUMBER_X = 100;
    private static final int RIGHT_NUMBER_X = 294;
    private static final int HEAD_Y = 14;
    private static final int RULE_Y = 24;
    private static final int BODY_Y = 30;
    private static final int BOTTOM_Y = 226;
    private static final int NUMBER_Y = 231;
    private static final int LINE = 9;
    private static final int GAP = 4;
    private static final int ROW = 10;
    private static final int GROUP_GAP = 3;
    private static final int ROWS_PER_SPREAD = 34;
    private static final int TAB_Y = 24;
    private static final int TAB_STEP = 34;
    private static final int ICON = 16;
    private static final int ICON_GAP = 4;
    private static final int CAPTION_GAP = 3;
    private static final String FIGURE = "!fig ";
    private static final String ITEMS = "!items ";
    private static final String[] NUMERALS = {"I.", "II.", "III.", "IV.", "V.", "VI.", "VII.", "VIII."};
    private static final int[] TAB_COLORS = {8004632, 5204787, 3100526, 9071142, 5914990, 4155242, 7232068, 1840140};

    sealed interface Op permits Rect, Text, Dots, Picture, Icon {}

    record Rect(int x, int y, int w, int h, int argb) implements Op {}

    record Text(int x, int y, FormattedCharSequence text, int rgb) implements Op {}

    record Dots(int x, int y, int w, int rgb) implements Op {}

    record Picture(int x, int y, int w, int h, ResourceLocation texture) implements Op {}

    record Icon(int x, int y, ItemStack stack) implements Op {}

    record Link(int x, int y, int w, int h, int spread) {}

    record Tab(int y, int rgb, int spread, String name) {}

    record Spread(List<Op> ops, List<Link> links, int group) {}

    private record Chapter(String group, String title, String shortTitle, List<String> paragraphs) {}

    private static final class Page {
        private final int x;
        private final int group;
        private final List<Op> ops = new ArrayList<>();
        private final List<Link> links = new ArrayList<>();
        private int y = BODY_Y;

        private Page(int x, int group) {
            this.x = x;
            this.group = group;
        }
    }

    private final Font font;
    private final List<Page> pages = new ArrayList<>();
    private final List<Spread> spreads = new ArrayList<>();
    private final List<Tab> tabs = new ArrayList<>();
    private int figures;

    private ManualBook(Font font, List<Chapter> chapters) {
        this.font = font;
        List<String> groups = chapters.stream().map(Chapter::group).distinct().toList();
        int rows = groups.size() + chapters.size();
        int contents = 2 * Math.max(1, (rows + ROWS_PER_SPREAD - 1) / ROWS_PER_SPREAD);
        for (int i = 0; i < contents; i++) {
            addPage("Field Manual", -1);
        }

        Map<Chapter, Integer> starts = new HashMap<>();
        for (Chapter chapter : chapters) {
            starts.put(chapter, this.pages.size());
            layOut(chapter, groups.indexOf(chapter.group()));
        }

        contents(groups, chapters, starts);
        for (int i = 0; i < groups.size(); i++) {
            String group = groups.get(i);
            int first = chapters.stream().filter(chapter -> chapter.group().equals(group)).mapToInt(starts::get).min().orElse(0);
            this.tabs.add(new Tab(TAB_Y + i * TAB_STEP, TAB_COLORS[i % TAB_COLORS.length], first / 2, group));
        }

        for (int i = 0; i < this.pages.size(); i += 2) {
            Page left = this.pages.get(i);
            Page right = i + 1 < this.pages.size() ? this.pages.get(i + 1) : null;
            List<Op> ops = new ArrayList<>(left.ops);
            List<Link> links = new ArrayList<>(left.links);
            if (right != null) {
                ops.addAll(right.ops);
                links.addAll(right.links);
            }
            this.spreads.add(new Spread(List.copyOf(ops), List.copyOf(links), left.group >= 0 || right == null ? left.group : right.group));
        }
    }

    static ManualBook load(Font font) {
        JsonArray entries = read(Minecraft.getInstance().getLanguageManager().getSelected());
        if (entries == null) {
            entries = read("en_us");
        }

        List<Chapter> chapters = new ArrayList<>();
        if (entries != null) {
            for (JsonElement entry : entries) {
                JsonObject object = entry.getAsJsonObject();
                List<String> paragraphs = new ArrayList<>();
                object.getAsJsonArray("text").forEach(paragraph -> paragraphs.add(paragraph.getAsString()));
                chapters.add(new Chapter(object.get("group").getAsString(), object.get("title").getAsString(), object.get("short").getAsString(), paragraphs));
            }
        }
        return new ManualBook(font, chapters);
    }

    @Nullable
    private static JsonArray read(String language) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(Airwaves.asResource("manual/" + language + ".json"));
        if (resource.isEmpty()) {
            return null;
        }

        try (Reader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (IOException | RuntimeException exception) {
            Airwaves.LOGGER.error("Failed to read the field manual for {}", language, exception);
            return null;
        }
    }

    List<Spread> spreads() {
        return this.spreads;
    }

    List<Tab> tabs() {
        return this.tabs;
    }

    private Page addPage(String head, int group) {
        int index = this.pages.size();
        boolean left = index % 2 == 0;
        Page page = new Page(left ? LEFT_X : RIGHT_X, group);
        page.ops.add(new Text(page.x, HEAD_Y, plain(head), INK2));
        page.ops.add(new Dots(page.x, RULE_Y, COLUMN, INK3));
        page.ops.add(new Text(left ? LEFT_NUMBER_X : RIGHT_NUMBER_X, NUMBER_Y, plain(Integer.toString(index + 1)), INK2));
        this.pages.add(page);
        return page;
    }

    private Page fit(Page page, String head, int height) {
        return page.y + height > BOTTOM_Y ? addPage(head, page.group) : page;
    }

    private void layOut(Chapter chapter, int group) {
        String head = chapter.group();
        Page page = addPage(head, group);
        for (FormattedCharSequence line : this.font.split(Component.literal(chapter.title()).withStyle(Style.EMPTY.withBold(true)), COLUMN)) {
            page.ops.add(new Text(page.x + (COLUMN - this.font.width(line)) / 2, page.y, line, RED));
            page.y += ROW;
        }
        page.y += GAP;

        List<String> paragraphs = chapter.paragraphs();
        for (int index = 0; index < paragraphs.size(); index++) {
            String paragraph = paragraphs.get(index);
            if (paragraph.startsWith("## ")) {
                page = fit(page, head, LINE * 2 + GAP);
                if (page.y > BODY_Y) {
                    page.y += GAP;
                }
                page.ops.add(new Text(page.x, page.y, Component.literal(paragraph.substring(3)).withStyle(Style.EMPTY.withBold(true)).getVisualOrderText(), RED));
                page.y += ROW;
                continue;
            }

            if (paragraph.startsWith(FIGURE) || paragraph.startsWith(ITEMS)) {
                page = figure(page, head, paragraph);
                continue;
            }

            boolean tip = paragraph.startsWith("> ");
            String mark = null;
            String text = paragraph;
            int indent = 0;
            if (tip) {
                text = paragraph.substring(2);
                indent = 5;
            } else if (paragraph.startsWith("- ")) {
                mark = "•";
                text = paragraph.substring(2);
                indent = 8;
            } else if (numberEnd(paragraph) > 0) {
                int end = numberEnd(paragraph);
                mark = paragraph.substring(0, end + 1);
                text = paragraph.substring(end + 2);
                indent = 11;
            }

            List<FormattedCharSequence> lines = this.font.split(rich(text), COLUMN - indent - (tip ? 2 : 0));
            if (tip || mark != null) {
                page = fit(page, head, lines.size() * LINE);
            }

            if (tip) {
                page.ops.add(new Rect(page.x, page.y - 2, COLUMN, lines.size() * LINE + 3, 0xFF000000 | SHADE));
                page.ops.add(new Rect(page.x, page.y - 2, 2, lines.size() * LINE + 3, 0xFF000000 | RED));
            }

            for (int i = 0; i < lines.size(); i++) {
                page = fit(page, head, LINE);
                if (i == 0 && mark != null) {
                    page.ops.add(new Text(page.x + (mark.length() > 1 ? 0 : 2), page.y, plain(mark), mark.length() > 1 ? RED : INK2));
                }
                page.ops.add(new Text(page.x + indent, page.y, lines.get(i), INK));
                page.y += LINE;
            }

            boolean listContinues = mark != null && index + 1 < paragraphs.size() && isListItem(paragraphs.get(index + 1));
            if (!listContinues) {
                page.y += GAP;
            }
        }
    }

    // Figure line !fig name | caption
    // Item row line !items id id | caption
    private Page figure(Page page, String head, String paragraph) {
        int split = paragraph.indexOf(" | ");
        String[] words = (split < 0 ? paragraph : paragraph.substring(0, split)).split(" ");
        String caption = split < 0 ? "" : paragraph.substring(split + 3);

        List<ItemStack> stacks = new ArrayList<>();
        ResourceLocation texture = null;
        int w;
        int h;
        if (paragraph.startsWith(ITEMS)) {
            for (int i = 1; i < words.length; i++) {
                ResourceLocation id = ResourceLocation.tryParse(words[i]);
                Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR) {
                    stacks.add(new ItemStack(item));
                }
            }
            if (stacks.isEmpty()) {
                return page;
            }
            w = stacks.size() * ICON + (stacks.size() - 1) * ICON_GAP;
            h = ICON;
        } else {
            texture = Airwaves.asResource("textures/gui/manual/fig/" + words[1] + ".png");
            int[] size = size(texture);
            if (size == null) {
                return page;
            }
            w = Math.min(size[0], COLUMN);
            h = size[1];
        }

        this.figures++;
        MutableComponent text = Component.empty()
                .append(Component.literal("Fig. " + this.figures + ". ").withStyle(Style.EMPTY.withColor(RED)))
                .append(rich(caption).copy().withStyle(Style.EMPTY.withItalic(true)));
        List<FormattedCharSequence> lines = this.font.split(text, COLUMN);
        page = fit(page, head, h + CAPTION_GAP + lines.size() * LINE);

        int x = page.x + (COLUMN - w) / 2;
        if (texture != null) {
            page.ops.add(new Picture(x, page.y, w, h, texture));
        }
        for (int i = 0; i < stacks.size(); i++) {
            page.ops.add(new Icon(x + i * (ICON + ICON_GAP), page.y, stacks.get(i)));
        }
        page.y += h + CAPTION_GAP;

        for (FormattedCharSequence line : lines) {
            page.ops.add(new Text(page.x + (COLUMN - this.font.width(line)) / 2, page.y, line, INK2));
            page.y += LINE;
        }
        page.y += GAP;
        return page;
    }

    @Nullable
    private static int[] size(ResourceLocation texture) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            Airwaves.LOGGER.warn("Missing field manual figure {}", texture);
            return null;
        }

        try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (IOException exception) {
            Airwaves.LOGGER.error("Failed to read field manual figure {}", texture, exception);
            return null;
        }
    }

    private void contents(List<String> groups, List<Chapter> chapters, Map<Chapter, Integer> starts) {
        Page first = this.pages.getFirst();
        FormattedCharSequence title = Component.literal("Contents").withStyle(Style.EMPTY.withBold(true)).getVisualOrderText();
        first.ops.add(new Text(first.x + (COLUMN - this.font.width(title)) / 2, first.y, title, RED));
        first.y += ROW + GAP;

        int index = 0;
        for (int i = 0; i < groups.size(); i++) {
            String group = groups.get(i);
            List<Chapter> members = chapters.stream().filter(chapter -> chapter.group().equals(group)).toList();
            Page page = this.pages.get(index);
            if (page.y + ROW > BOTTOM_Y) {
                page = this.pages.get(++index);
            }

            page.ops.add(new Text(page.x, page.y, plain(NUMERALS[i % NUMERALS.length]), RED));
            row(page, 20, group, starts.get(members.getFirst()), RED);
            for (Chapter chapter : members) {
                if (page.y + ROW > BOTTOM_Y) {
                    page = this.pages.get(++index);
                }
                row(page, 12, chapter.shortTitle(), starts.get(chapter), INK);
            }
            page.y += GROUP_GAP;
        }
    }

    private void row(Page page, int indent, String title, int target, int rgb) {
        FormattedCharSequence text = plain(title);
        FormattedCharSequence number = plain(Integer.toString(target + 1));
        int textEnd = page.x + indent + this.font.width(text);
        int numberX = page.x + COLUMN - this.font.width(number);
        page.ops.add(new Text(page.x + indent, page.y, text, rgb));
        page.ops.add(new Dots(textEnd + 3, page.y + 6, numberX - textEnd - 6, INK3));
        page.ops.add(new Text(numberX, page.y, number, INK2));
        page.links.add(new Link(page.x, page.y - 1, COLUMN, ROW, target / 2));
        page.y += ROW;
    }

    private static FormattedCharSequence plain(String text) {
        return Component.literal(text).getVisualOrderText();
    }

    private static Component rich(String text) {
        MutableComponent out = Component.empty();
        boolean bold = false;
        for (String part : text.split("\\*\\*", -1)) {
            if (!part.isEmpty()) {
                out.append(Component.literal(part).withStyle(Style.EMPTY.withBold(bold)));
            }
            bold = !bold;
        }
        return out;
    }

    private static int numberEnd(String paragraph) {
        int dot = paragraph.indexOf(". ");
        return dot > 0 && dot <= 2 && paragraph.substring(0, dot).chars().allMatch(Character::isDigit) ? dot : -1;
    }

    private static boolean isListItem(String paragraph) {
        return paragraph.startsWith("- ") || numberEnd(paragraph) > 0;
    }
}
