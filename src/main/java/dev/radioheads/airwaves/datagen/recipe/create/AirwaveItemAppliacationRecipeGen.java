package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public final class AirwaveItemAppliacationRecipeGen extends ItemApplicationRecipeGen {
    public AirwaveItemAppliacationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe
        SILVER_CASING = create("silver_casing", b -> b
            .require(AllBlocks.BRASS_CASING)
            .require(AirwaveMaterials.SILVER.ingots)
            .output(AirwaveBlocks.SILVER_CASING)
    );
}
