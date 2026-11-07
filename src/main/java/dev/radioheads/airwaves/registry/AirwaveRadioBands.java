package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.registry.radio.RadioBandEntry;

public class AirwaveRadioBands {
    public static final RadioBandEntry
            AM_BROADCAST = Airwaves.REGISTRATE.radioBand("am_broadcast",
                    () -> new RadioBand(530_000L, 1_700_000L, 10_000L, 1_000_000L, "AM", AirwaveModulations.AM)).register(),

            HF = Airwaves.REGISTRATE.radioBand("hf",
                    () -> new RadioBand(3_000_000L, 30_000_000L, 1_000L, 7_100_000L, "HF", AirwaveModulations.SSB)).register(),

            FM_BROADCAST = Airwaves.REGISTRATE.radioBand("fm_broadcast",
                    () -> new RadioBand(87_500_000L, 108_000_000L, 100_000L, 98_100_000L, "FM", AirwaveModulations.WIDE_FM)).register()
    ;

    public static void init() {}
}
