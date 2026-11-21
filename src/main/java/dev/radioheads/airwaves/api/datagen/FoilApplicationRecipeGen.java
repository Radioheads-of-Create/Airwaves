package dev.radioheads.airwaves.api.datagen;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import dev.radioheads.airwaves.api.recipe.foil.FoilApplicationRecipeParams;
import dev.radioheads.airwaves.content.recipe.FoilApplicationRecipe;
import dev.radioheads.airwaves.registry.AirwaveRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public abstract class FoilApplicationRecipeGen extends ProcessingRecipeGen<FoilApplicationRecipeParams, FoilApplicationRecipe, FoilApplicationRecipe.Builder<FoilApplicationRecipe>> {
    public FoilApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
        super(output, registries, defaultNamespace);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return AirwaveRecipeTypes.FOIL_APPLICATION;
    }

    @Override
    protected FoilApplicationRecipe.Builder<FoilApplicationRecipe> getBuilder(ResourceLocation id) {
        return new FoilApplicationRecipe.Builder<>(FoilApplicationRecipe::new, id);
    }
}
