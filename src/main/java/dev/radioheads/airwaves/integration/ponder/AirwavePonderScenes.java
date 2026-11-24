package dev.radioheads.airwaves.integration.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.integration.ponder.scenes.RadioScenes;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AirwavePonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?,?>> HELPER = helper.withKeyFunction((RegistryEntry::getId));

        HELPER.forComponents(AirwaveBlocks.RADIO).addStoryBoard("radio", RadioScenes::radio, AirwavePonderTags.RADIO);
    }
}
