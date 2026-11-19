package dev.radioheads.airwaves.config;

import dev.radioheads.airwaves.config.client.AirwaveDevConfig;
import net.createmod.catnip.config.ConfigBase;

public class AirwaveClientConfig extends ConfigBase {
    public final AirwaveDevConfig devConfig = nested(0, AirwaveDevConfig::new, "Dev client debug options");

    @Override
    public String getName() {
        return "client";
    }
}
