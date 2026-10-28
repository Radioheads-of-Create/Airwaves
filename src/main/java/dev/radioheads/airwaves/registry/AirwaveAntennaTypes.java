package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.antenna.AntennaType;
import dev.radioheads.airwaves.api.antenna.PowerLawRange;
import dev.radioheads.airwaves.api.antenna.model.QualityModel;
import dev.radioheads.airwaves.api.antenna.pattern.CosinePattern;
import dev.radioheads.airwaves.api.registry.antenna.AntennaTypeEntry;
import dev.radioheads.airwaves.data.AirwaveTags;

public final class AirwaveAntennaTypes {
    private AirwaveAntennaTypes() {}

    public static final AntennaTypeEntry BASIC = Airwaves.REGISTRATE.antennaType("basic", () -> new AntennaType(
            s -> s.is(AirwaveTags.Blocks.STANDARD_ANTENNA.tag),
            96,
            AirwaveTags.Tiers.STANDARD.tag,
            new PowerLawRange(600.0, 4, 20, 1.13, 0.75),
            QualityModel.DEFAULT,
            null
    )).register();

    public static final AntennaTypeEntry YAGI = Airwaves.REGISTRATE.antennaType("yagi", () -> new AntennaType(
            s -> s.is(AirwaveTags.Blocks.STANDARD_ANTENNA.tag),
            96,
            AirwaveTags.Tiers.STANDARD.tag,
            new PowerLawRange(600.0, 4, 20, 1.13, 0.75),
            QualityModel.DEFAULT,
            new CosinePattern(1.9, 60.0)
    )).register();

    public static void init() {}
}
