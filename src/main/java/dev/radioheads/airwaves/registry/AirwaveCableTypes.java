package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.registry.cable.CableTypeEntry;

public final class AirwaveCableTypes {
    private AirwaveCableTypes() {}

    public static final CableTypeEntry
        COPPER = Airwaves.REGISTRATE.cableType("copper", () -> new CableType(AirwaveItems.COPPER_CABLE_SPOOL, 0.00505)).register(),
        GOLD = Airwaves.REGISTRATE.cableType("gold", () -> new CableType(AirwaveItems.GOLD_CABLE_SPOOL, 0.00611)).register(),
        SILVER = Airwaves.REGISTRATE.cableType("silver", () -> new CableType(AirwaveItems.SILVER_CABLE_SPOOL, 0.00491)).register()
    ;

    public static void init() {}
}
