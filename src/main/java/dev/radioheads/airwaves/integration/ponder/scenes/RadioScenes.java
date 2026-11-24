package dev.radioheads.airwaves.integration.ponder.scenes;

import dev.radioheads.airwaves.integration.ponder.AirwaveSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;

public class RadioScenes {
    public static void radio(SceneBuilder builder, SceneBuildingUtil util) {
        AirwaveSceneBuilder scene = new AirwaveSceneBuilder(builder);
        scene.title("radio", "Radio");
        scene.configureBasePlate(0,0,7);
        scene.showBasePlate();

        Selection radio = util.select().fromTo(1,1, 1, 1, 1, 1);

        ElementLink<WorldSectionElement> radioElement = scene.world().showIndependentSection(radio, Direction.DOWN);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("This is a radio");

        scene.idle(60);
    }
}
