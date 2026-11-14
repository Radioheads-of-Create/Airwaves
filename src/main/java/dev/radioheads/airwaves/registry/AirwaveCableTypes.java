package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.registry.cable.CableTypeEntry;
import dev.radioheads.airwaves.api.util.ColorUtil;

public final class AirwaveCableTypes {
    private AirwaveCableTypes() {}

    public static final CableTypeEntry
        COPPER = Airwaves.REGISTRATE.cableType("copper", () -> new CableType(AirwaveItems.COPPER_CABLE_SPOOL, 0.00505, ColorUtil.rgb(184, 115, 51))).register(),
        GOLD = Airwaves.REGISTRATE.cableType("gold", () -> new CableType(AirwaveItems.GOLD_CABLE_SPOOL, 0.00611, ColorUtil.rgb(255, 215, 0))).register(),
        SILVER = Airwaves.REGISTRATE.cableType("silver", () -> new CableType(AirwaveItems.SILVER_CABLE_SPOOL, 0.00491, ColorUtil.rgb(192, 192, 192))).register(),
        CREATIVE = Airwaves.REGISTRATE.cableType("creative", () -> new CableType(AirwaveItems.CREATIVE_CABLE_SPOOL, 0.0, ColorUtil.rgb(192, 192, 192))).register()
    ;

    public static void init() {}
}
