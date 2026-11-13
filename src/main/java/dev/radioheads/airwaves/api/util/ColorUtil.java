package dev.radioheads.airwaves.api.util;

import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.radio.Tuning;
import net.createmod.catnip.theme.Color;

public final class ColorUtil {
    private ColorUtil() {}

    public static int colorFromTune(Tuning tuning) {
        RadioBand b = tuning.band().value();
        float t = (float) (b.clamp(tuning.hertz()) - b.minHertz()) / (b.maxHertz() - b.minHertz());
        // Conflicts with catnip Color class
        return java.awt.Color.HSBtoRGB(Math.clamp(t, 0f, 1f) * 0.75f, 1f, 1f) & 0xFFFFFF;
    }

    public static int rgb(int r, int g, int b) {
        return argb(0xFF, r, g, b);
    }

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Color toCatnipColor(int rgb) {
        return new Color(rgb & 0xFFFFFF, false);
    }
}
