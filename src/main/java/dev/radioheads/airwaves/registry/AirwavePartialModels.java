package dev.radioheads.airwaves.registry;

import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.radioheads.airwaves.Airwaves;

public final class AirwavePartialModels {
    private AirwavePartialModels() {}

    public static final PartialModel
        FOIL_ROLL = block("foil/roll");

    private static PartialModel block(String path) {
        return PartialModel.of(Airwaves.asResource("block/" + path));
    }

    public static void init() {}
}
