package dev.radioheads.airwaves.integration.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;

public class AirwaveSceneBuilder extends CreateSceneBuilder {
    private final AirwaveWorldInstructions airwaveInstructions;

    public AirwaveSceneBuilder(SceneBuilder baseSceneBuilder) {
        super(baseSceneBuilder);
        airwaveInstructions = new AirwaveWorldInstructions();
    }

    public AirwaveWorldInstructions airwaveInstructions() {
        return airwaveInstructions;
    }

    public class AirwaveWorldInstructions extends WorldInstructions {

    }
}
