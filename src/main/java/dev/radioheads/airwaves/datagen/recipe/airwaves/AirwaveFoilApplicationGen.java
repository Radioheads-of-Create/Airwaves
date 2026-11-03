package dev.radioheads.airwaves.datagen.recipe.airwaves;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.datagen.FoilApplicationRecipeGen;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public final class AirwaveFoilApplicationGen extends FoilApplicationRecipeGen {
    public AirwaveFoilApplicationGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe TEST = create("test", b -> b
            .require(Items.DIRT)
            .require(AirwaveItems.COPPER_FOIL)
            .output(Items.GRASS_BLOCK)
            .duration(100)
    );
}
