package dev.radioheads.airwaves.integration.ponder.scenes;

import dev.radioheads.airwaves.integration.ponder.AirwaveSceneBuilder;
import dev.radioheads.airwaves.integration.ponder.elements.RadioWaveOverlay;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;

public class RadioScenes {
    public static void radio(SceneBuilder builder, SceneBuildingUtil util) {
        AirwaveSceneBuilder scene = new AirwaveSceneBuilder(builder);
        RadioWaveOverlay waves = new RadioWaveOverlay();
        scene.title("radio", "Radio");
        scene.configureBasePlate(0,0,9);
        scene.showBasePlate();

        scene.addInstruction(s -> s.addElement(waves));

        scene.idle(80);

        scene.addInstruction(s -> waves.setVisible(false));

    }
}
