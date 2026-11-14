package dev.radioheads.airwaves.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.content.block.HfTransceiverControl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class AirwavePartialModels {
    private AirwavePartialModels() {}

    public static final PartialModel
        FOIL_ROLL = block("foil_applicator/roll"),
        FOIL_ROLL_BLOCK = block("foil_applicator/roll_block"),
        MICROPHONE_KNOB = block("microphone/knob");

    // Telephone dial buttons
    public static final PartialModel[] TELEPHONE_BUTTONS = new PartialModel[9];

    static {
        TELEPHONE_BUTTONS[0] = block("telephone/button_call");
        for (int digit = 1; digit < TELEPHONE_BUTTONS.length; digit++) {
            TELEPHONE_BUTTONS[digit] = block("telephone/button_" + digit);
        }
    }

    // HF transceiver controls and lights by finish
    private static final Map<HfTransceiverBlock.Finish, Map<String, PartialModel>> HF_TRANSCEIVER = new EnumMap<>(HfTransceiverBlock.Finish.class);

    static {
        for (HfTransceiverBlock.Finish finish : HfTransceiverBlock.Finish.values()) {
            Map<String, PartialModel> parts = new HashMap<>();
            for (HfTransceiverControl control : HfTransceiverControl.values()) {
                parts.put(control.id(), hfPart(finish, control.id()));
            }
            for (String led : HfTransceiverControl.LEDS) {
                parts.put(led, hfPart(finish, led));
            }
            HF_TRANSCEIVER.put(finish, parts);
        }
    }

    private static PartialModel block(String path) {
        return PartialModel.of(Airwaves.asResource("block/" + path));
    }

    private static PartialModel hfPart(HfTransceiverBlock.Finish finish, String name) {
        return block("hf_transceiver/" + finish.getSerializedName() + "/" + name);
    }

    public static PartialModel hfTransceiver(HfTransceiverBlock.Finish finish, String part) {
        return HF_TRANSCEIVER.get(finish).get(part);
    }

    public static void init() {}
}
