package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.registry.modulation.ModulationEntry;

public final class AirwaveModulations {
    private AirwaveModulations() {}

    public static final ModulationEntry
            AM = Airwaves.REGISTRATE.modulation("am", () -> new Modulation(9_000, 4_500, 10.0, 25.0, 100.0)).register(),
            SSB = Airwaves.REGISTRATE.modulation("ssb", () -> new Modulation(2_700, 2_700, 6.0, 20.0, 100.0)).register(),
            WIDE_FM = Airwaves.REGISTRATE.modulation("wide_fm", () -> new Modulation(150_000, 12_000, 12.0, 6.0, 6.0)).register()
    ;

    public static void init() {}
}
