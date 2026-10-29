package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.api.data.recipe.PressingRecipeGen;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public final class AirwavePressingRecipeGen extends PressingRecipeGen {
    public AirwavePressingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe SILVER_SHEET = create("silver_sheet", b -> b
            .require(AirwaveMaterials.SILVER.ingots)
            .output(AirwaveItems.SILVER_SHEET));
}
