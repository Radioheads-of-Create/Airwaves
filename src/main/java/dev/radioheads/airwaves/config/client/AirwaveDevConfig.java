package dev.radioheads.airwaves.config.client;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AirwaveDevConfig extends ConfigBase {
    private ModConfigSpec.BooleanValue nodeDebugRendering;

    @Override
    public void registerAll(ModConfigSpec.Builder builder) {
        builder.push("rendering");

        nodeDebugRendering = builder
                .comment("Enables debug rendering for nodes")
                .define("node_debug_rendering", false);

        builder.pop();

        super.registerAll(builder);
    }

    public boolean nodeDebugRendering() {
        return nodeDebugRendering.get();
    }

    @Override
    public String getName() {
        return "dev_config";
    }
}
