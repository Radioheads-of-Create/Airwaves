package dev.radioheads.airwaves.integration.jei;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
@NothingNullByDefault
@SuppressWarnings("unused")
public class AirwaveJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = Airwaves.asResource("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}
