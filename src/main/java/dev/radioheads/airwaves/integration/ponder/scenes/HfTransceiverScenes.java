package dev.radioheads.airwaves.integration.ponder.scenes;

import dev.radioheads.airwaves.integration.ponder.AirwaveSceneBuilder;
import dev.radioheads.airwaves.integration.ponder.elements.RadioWaveOverlay;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public class HfTransceiverScenes {
    public static void hfTransceiver(SceneBuilder builder, SceneBuildingUtil util) {
        AirwaveSceneBuilder scene = new AirwaveSceneBuilder(builder);
        RadioWaveOverlay waves = new RadioWaveOverlay();
        scene.title("hf_transceiver", "HF Transceiver");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();

        scene.idle(10);

    }
}
