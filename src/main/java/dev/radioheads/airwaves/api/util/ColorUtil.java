package dev.radioheads.airwaves.api.util;

import dev.radioheads.airwaves.api.radio.Frequency;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.radio.Tuning;

import java.awt.*;

public final class ColorUtil {
    private ColorUtil() {}

    public static int colorFromTune(Tuning tuning) {
        RadioBand b = tuning.band().value();
        Frequency f = b.clamp(tuning.frequency());

        double min = b.min().toHz();
        double span = b.max().toHz() - min;
        double t = span > 0.0 ? (f.toHz() - min) / span : 0.0;

        float hue = (float) Math.clamp(t, 0.0, 1.0) * 0.75f;
        return Color.HSBtoRGB(hue, 1f, 1f) & 0xFFFFFF;
    }
}