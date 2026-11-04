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
                    87.5,
                    108.0,
                    HertzUnit.MHz,
                    AirwaveModulations.FM,
                    98.1
            )).register(),

    AM = Airwaves.REGISTRATE.radioBand("am",
            () -> new RadioBand(
                    530.0,
                    1700.0,
                    HertzUnit.kHz,
                    AirwaveModulations.AM,
                    1000.0
            )).register()
            ;

    public static void init() {}
}
