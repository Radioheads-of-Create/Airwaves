package dev.radioheads.airwaves.datagen;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.registry.AirwaveBiomeModifiers;
import dev.radioheads.airwaves.registry.AirwaveConfiguredFeatures;
import dev.radioheads.airwaves.registry.AirwavePlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AirwaveGeneratedEntriesProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, AirwaveConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, AirwavePlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AirwaveBiomeModifiers::bootstrap);

    public AirwaveGeneratedEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Airwaves.MOD_ID));
    }

    @Override
    public String getName() {
        return "Airwaves' Generated Registry Entries";
    }
}
