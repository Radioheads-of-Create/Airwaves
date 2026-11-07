package dev.radioheads.airwaves.api.util;

import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.radio.Tuning;

import java.awt.*;

public final class ColorUtil {
    private ColorUtil() {}

    public static int colorFromTune(Tuning tuning) {
        RadioBand b = tuning.band().value();
        float t = (float) (b.clamp(tuning.hertz()) - b.minHertz()) / (b.maxHertz() - b.minHertz());
        return Color.HSBtoRGB(Math.clamp(t, 0f, 1f) * 0.75f, 1f, 1f) & 0xFFFFFF;
    }
}
