package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Frequency;
import dev.radioheads.airwaves.api.radio.HertzUnit;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.registry.radio.RadioBandEntry;

public class AirwaveRadioBands {
    public static final RadioBandEntry
            FM = Airwaves.REGISTRATE.radioBand("fm",
            () -> new RadioBand(
                    Frequency.of(87.5, HertzUnit.MHz),
                    Frequency.of(108.0, HertzUnit.MHz),
                    Frequency.of(98.1, HertzUnit.MHz)
            ).withModulation(AirwaveModulations.FM)).register(),

    AM = Airwaves.REGISTRATE.radioBand("am",
            () -> new RadioBand(
                    Frequency.of(530.0, HertzUnit.kHz),
                    Frequency.of(1700.0, HertzUnit.kHz),
                    Frequency.of(1000.0, HertzUnit.kHz)
            ).withModulation(AirwaveModulations.AM)).register()
    ;

    public static void init() {}
}
