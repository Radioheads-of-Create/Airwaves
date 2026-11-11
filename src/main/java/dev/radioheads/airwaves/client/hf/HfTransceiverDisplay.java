package dev.radioheads.airwaves.client.hf;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class HfTransceiverDisplay {
    public static final int WIDTH = 140;
    public static final int HEIGHT = 60;
    public static final Palette GREEN = new Palette(0xFF3EF0AB, 0xFF62F7C0, 0xFF13241E, 0xFF03261B);
    public static final Palette AMBER = new Palette(0xFFE9A646, 0xFFF0AC4C, 0xFF2C2924, 0xFF3A2207);

    private static final int TOP = 3, NUMBERS = 11, BAR = 18, BAR_HEIGHT = 5, BUSY = 38, BOTTOM = HEIGHT - 8;
    private static final int DIGIT_WIDTH = 9, DIGIT_HEIGHT = 22, STROKE = 3, DIGIT_TOP = 13, SEGMENTS = 15;
    private static final float GHOST = 0.09F;
    private static final Map<Character, String> SEVEN = Map.ofEntries(
            Map.entry('0', "abcdef"), Map.entry('1', "bc"), Map.entry('2', "abged"), Map.entry('3', "abgcd"), Map.entry('4', "fgbc"),
            Map.entry('5', "afgcd"), Map.entry('6', "afgedc"), Map.entry('7', "abc"), Map.entry('8', "abcdefg"), Map.entry('9', "abcdfg"), Map.entry(' ', ""));
    private static final Map<Character, String> FONT = Map.ofEntries(
            Map.entry('A', "010101111101101"), Map.entry('B', "110101110101110"), Map.entry('C', "011100100100011"), Map.entry('D', "110101101101110"),
            Map.entry('E', "111100110100111"), Map.entry('F', "111100110100100"), Map.entry('G', "011100101101011"), Map.entry('H', "101101111101101"),
            Map.entry('I', "111010010010111"), Map.entry('K', "101101110101101"), Map.entry('L', "100100100100111"), Map.entry('M', "101111111101101"),
            Map.entry('N', "110101101101101"), Map.entry('O', "010101101101010"), Map.entry('P', "110101110100100"), Map.entry('R', "110101110101101"),
            Map.entry('S', "011100010001110"), Map.entry('T', "111010010010010"), Map.entry('U', "101101101101111"), Map.entry('V', "101101101101010"),
            Map.entry('W', "101101111111101"), Map.entry('X', "101101010101101"), Map.entry('Y', "101101010010010"), Map.entry('Z', "111001010100111"),
            Map.entry('0', "111101101101111"), Map.entry('1', "010110010010111"), Map.entry('2', "110001010100111"), Map.entry('3', "110001010001110"),
            Map.entry('4', "101101111001001"), Map.entry('5', "111100110001110"), Map.entry('6', "011100111101111"), Map.entry('7', "111001010010010"),
            Map.entry('8', "111101111101111"), Map.entry('9', "111101111001110"), Map.entry('.', "000000000000010"), Map.entry('-', "000000111000000"),
            Map.entry(' ', "000000000000000"));

    public record Palette(int background, int transmit, int off, int ink) {}

    public record Frame(boolean powered, boolean transmitting, boolean slotB, boolean locked, boolean fast, boolean megahertz,
                        int meter, boolean busy, long hertz, int watts, boolean wattsHidden,
                        @Nullable String antenna, @Nullable String loss, int volumePercent, Palette palette) {}

    private HfTransceiverDisplay() {}

    public static void paint(int[] pixels, Frame frame) {
        Palette palette = frame.palette();
        int background = !frame.powered() ? palette.off() : frame.transmitting() ? palette.transmit() : palette.background();
        java.util.Arrays.fill(pixels, background);
        if (!frame.powered()) {
            return;
        }
        Canvas canvas = new Canvas(pixels, palette.ink(), mix(background, palette.ink(), GHOST));

        canvas.text("A", 3, TOP, !frame.slotB());
        canvas.text("B", 8, TOP, frame.slotB());
        canvas.text("LOCK", 60, TOP, frame.locked());
        canvas.text("FAST", 103, TOP, frame.fast());
        canvas.text("1MHZ", 122, TOP, frame.megahertz());

        boolean antenna = frame.antenna() != null;
        boolean volume = !antenna && !frame.transmitting() && frame.volumePercent() >= 0;
        if (antenna) {
            canvas.text(frame.antenna(), 3, NUMBERS, true);
            canvas.text(frame.loss() == null ? "" : frame.loss(), 3, BAR, true);
        } else {
            int level = frame.transmitting() ? Math.min(SEGMENTS, Math.round(frame.watts() / 100.0F * SEGMENTS))
                    : volume ? Math.round(frame.volumePercent() / 100.0F * SEGMENTS) : frame.meter();
            for (int i = 0; i < SEGMENTS; i++) {
                int height = volume || frame.transmitting() || i >= 9 ? BAR_HEIGHT : BAR_HEIGHT - 1;
                canvas.rect(3 + i * 3, BAR + BAR_HEIGHT - height, 2, height, i < level);
            }
            if (frame.transmitting()) {
                canvas.text("PO", 3, NUMBERS, true);
            } else if (volume) {
                canvas.text("AF " + String.format("%3d", frame.volumePercent()), 3, NUMBERS, true);
            } else {
                String[] labels = {"1", "3", "5", "7", "9", "20", "60"};
                int[] at = {0, 2, 4, 6, 8, 10, 14};
                for (int i = 0; i < labels.length; i++) {
                    canvas.text(labels[i], 3 + at[i] * 3, NUMBERS, true);
                }
            }
        }
        if (frame.busy() && !frame.transmitting() && !antenna) {
            canvas.fill(3, BUSY, 19, 7, palette.ink());
            canvas.text("BUSY", 5, BUSY + 1, background);
        }

        long megahertz = frame.hertz() / 1_000_000L;
        long kilohertz = frame.hertz() / 1_000L % 1_000L;
        String digits = (megahertz >= 10 ? String.valueOf(megahertz / 10) : " ") + (megahertz % 10) + "." + String.format("%03d", kilohertz);
        int dot = STROKE + 2, step = DIGIT_WIDTH + 2;
        int width = 0;
        for (char c : digits.toCharArray()) {
            width += c == '.' ? dot : step;
        }
        int x = WIDTH - 3 - width + 2;
        for (char c : digits.toCharArray()) {
            if (c == '.') {
                canvas.fill(x, DIGIT_TOP + DIGIT_HEIGHT - STROKE, STROKE, STROKE, palette.ink());
                x += dot;
            } else {
                canvas.digit(c, x, DIGIT_TOP);
                x += step;
            }
        }

        canvas.text("SSB", 62, BOTTOM, true);
        String watts = frame.watts() + "W";
        canvas.text(watts, WIDTH - 3 - watts.length() * 4 + 1, BOTTOM, !frame.wattsHidden());
    }

    private static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    private record Canvas(int[] pixels, int ink, int ghost) {
        void fill(int x, int y, int w, int h, int color) {
            for (int j = Math.max(0, y); j < Math.min(HEIGHT, y + h); j++) {
                for (int i = Math.max(0, x); i < Math.min(WIDTH, x + w); i++) {
                    this.pixels[j * WIDTH + i] = color;
                }
            }
        }

        void rect(int x, int y, int w, int h, boolean on) {
            fill(x, y, w, h, on ? this.ink : this.ghost);
        }

        void text(String text, int x, int y, boolean on) {
            text(text, x, y, on ? this.ink : this.ghost);
        }

        void text(String text, int x, int y, int color) {
            for (char c : text.toCharArray()) {
                String glyph = FONT.getOrDefault(c, FONT.get(' '));
                for (int i = 0; i < 15; i++) {
                    if (glyph.charAt(i) == '1') {
                        fill(x + i % 3, y + i / 3, 1, 1, color);
                    }
                }
                x += 4;
            }
        }

        void digit(char c, int x, int y) {
            int half = DIGIT_HEIGHT / 2, lower = y + half + (STROKE + 1) / 2, lowerHeight = DIGIT_HEIGHT - half - (STROKE + 1) / 2 - STROKE;
            int[][] segments = {
                    {x + STROKE, y, DIGIT_WIDTH - 2 * STROKE, STROKE},
                    {x + DIGIT_WIDTH - STROKE, y + STROKE, STROKE, half - STROKE},
                    {x + DIGIT_WIDTH - STROKE, lower, STROKE, lowerHeight},
                    {x + STROKE, y + DIGIT_HEIGHT - STROKE, DIGIT_WIDTH - 2 * STROKE, STROKE},
                    {x, lower, STROKE, lowerHeight},
                    {x, y + STROKE, STROKE, half - STROKE},
                    {x + STROKE, y + half - STROKE / 2, DIGIT_WIDTH - 2 * STROKE, STROKE}
            };
            String on = SEVEN.getOrDefault(c, "");
            for (int i = 0; i < segments.length; i++) {
                int[] s = segments[i];
                rect(s[0], s[1], s[2], s[3], on.indexOf((char) ('a' + i)) >= 0);
            }
        }
    }
}
