package dev.radioheads.airwaves.datagen;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.datagen.recipe.create.AirwaveCuttingRecipeGen;
import dev.radioheads.airwaves.datagen.recipe.create.AirwavePolishingRecipeGen;
import dev.radioheads.airwaves.datagen.recipe.create.AirwavePressingRecipeGen;
import dev.radioheads.airwaves.datagen.recipe.create.AirwaveSequencedAssemblyRecipeGen;
import dev.radioheads.airwaves.datagen.recipe.minecraft.MinecraftRecipeGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class AirwaveDataGen {
    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        var registries = event.getLookupProvider();

        event.addProvider(new AirwavePolishingRecipeGen(output, registries));
        event.addProvider(new AirwaveGeneratedEntriesProvider(output, registries));
        event.addProvider(new AirwaveCuttingRecipeGen(output, registries));
        event.addProvider(new AirwavePressingRecipeGen(output, registries));
        event.addProvider(new AirwaveSequencedAssemblyRecipeGen(output, registries));

        event.addProvider(new MinecraftRecipeGen(output, registries));
    }
}
