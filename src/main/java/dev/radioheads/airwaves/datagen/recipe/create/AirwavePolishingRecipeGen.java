package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.api.data.recipe.PolishingRecipeGen;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public final class AirwavePolishingRecipeGen extends PolishingRecipeGen {
    public AirwavePolishingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe CUT_AMETHYST = create("cut_amethyst", b -> b
            .require(Items.AMETHYST_SHARD)
            .output(AirwaveItems.CUT_AMETHYST)
    );
}
