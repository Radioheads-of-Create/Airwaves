package dev.radioheads.airwaves.api.radio.system;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.radio.Frequency;

// Who said we can't have realistic radio
@NothingNullByDefault
public final class RadioCalculation {
    private RadioCalculation() {}

    // Also known as wavelength!
    public static double lambda(Frequency frequency, int blocks) {
        return blocks * (300 / frequency.toMHz());
    }
}
