package dev.radioheads.airwaves.integration.svc;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;

@ForgeVoicechatPlugin
// The entire mod relies on this
public class AirwavesVoicechatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return "airwaves";
    }
}
