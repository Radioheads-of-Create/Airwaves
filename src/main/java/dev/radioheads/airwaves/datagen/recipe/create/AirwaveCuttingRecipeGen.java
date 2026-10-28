package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.api.data.recipe.CuttingRecipeGen;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public final class AirwaveCuttingRecipeGen extends CuttingRecipeGen {
    public AirwaveCuttingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe
        COPPER_WIRE = create("copper_wire", b -> b
            .require(AirwaveMaterials.COPPER.plates)
            .output(AirwaveItems.COPPER_WIRE.asStack(4))
            .duration(100)
    ),

    GOLD_WIRE = create("gold_wire", b -> b
                .require(AirwaveMaterials.GOLD.plates)
                .output(AirwaveItems.GOLD_WIRE.asStack(4))
                .duration(100)
    ),

    SILVER_WIRE = create("silver_wire", b -> b
            .require(AirwaveMaterials.SILVER.plates)
            .output(AirwaveItems.SILVER_WIRE.asStack(4))
            .duration(100)
    );
}
