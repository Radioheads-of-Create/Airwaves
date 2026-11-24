package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.registry.AirwaveFluids;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;

public final class AirwaveMixingRecipeGen extends MixingRecipeGen {
    public AirwaveMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe SALT_SOLUTION = create("salt_solution", b -> b
            .require(AirwaveItems.ROCK_SALT)
            .require(Fluids.WATER, 250)
            .output(AirwaveFluids.SALT_SOLUTION.get(), 250)
            .requiresHeat(HeatCondition.HEATED)
    );
}



