package dev.radioheads.airwaves.integration.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class AirwavePonderTags {
    public static final ResourceLocation RADIO = Airwaves.asResource("radio");
    public static final ResourceLocation HF_TRANSCEIVER = Airwaves.asCommon("hf_transceiver");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(RegisteredObjectsHelper::getKeyOrThrow);

        helper.registerTag(RADIO)
                .addToIndex()
                .item(AirwaveBlocks.RADIO, true, false)
                .title("Radio.")
                .description("Just a Radio.")
                .register();

        helper.registerTag(HF_TRANSCEIVER)
                .addToIndex()
                .item(AirwaveBlocks.HF_TRANSCEIVER)
                .title("Transceiver")
                .description("Transceivers")
                .register();

        HELPER.addToTag(RADIO)
                .add(AirwaveBlocks.RADIO);

        HELPER.addToTag(HF_TRANSCEIVER)
                .add(AirwaveBlocks.HF_TRANSCEIVER);
    }
}
