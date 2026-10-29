package dev.radioheads.airwaves.datagen;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.datagen.advancement.AirwaveAdvancementGen;
import dev.radioheads.airwaves.datagen.recipe.create.*;
import dev.radioheads.airwaves.datagen.recipe.minecraft.MinecraftRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class AirwaveDataGen {
    private static final List<ProviderFactory<?>> PROVIDERS = List.of(
            AirwavePolishingRecipeGen::new,
            AirwaveGeneratedEntriesProvider::new,
            AirwaveCuttingRecipeGen::new,
            AirwavePressingRecipeGen::new,
            AirwaveSequencedAssemblyRecipeGen::new,
            AirwaveItemAppliacationRecipeGen::new,
            MinecraftRecipeGen::new
    );

    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        event.addProvider(new AirwaveAdvancementGen(output, registries, existingFileHelper));


        for (ProviderFactory<?> factory : PROVIDERS) {
            event.addProvider(factory.create(output, registries));
        }
    }

    @FunctionalInterface
    private interface ProviderFactory<T extends DataProvider> {
        T create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries);
    }
}