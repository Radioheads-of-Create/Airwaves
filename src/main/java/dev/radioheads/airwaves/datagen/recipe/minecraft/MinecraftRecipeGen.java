package dev.radioheads.airwaves.datagen.recipe.minecraft;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

@NothingNullByDefault
public class MinecraftRecipeGen extends RecipeProvider {
    public MinecraftRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput out) {
        generateCookingRecipes(out);
        generateShapedCrafting(out);
        generateShapelessCrafting(out);
    }

    private void generateCookingRecipes(RecipeOutput out) {
        SimpleCookingRecipeBuilder.blasting(
                Ingredient.of(AirwaveMaterials.SILVER.rawMaterial),
                RecipeCategory.MISC,
                AirwaveItems.SILVER_INGOT.asStack(),
                0.1f,
                200
        )
                .unlockedBy("has_raw_silver", has(AirwaveMaterials.SILVER.rawMaterial))
                .save(out);

        SimpleCookingRecipeBuilder.blasting(
                Ingredient.of(AirwaveMaterials.SILVER.rawStorageBlocks.items()),
                RecipeCategory.MISC,
                AirwaveItems.SILVER_INGOT.asStack(),
                0.1f,
                200
        )
                .unlockedBy("has_raw_silver", has(AirwaveMaterials.SILVER.rawStorageBlocks.items()))
                .save(out, "silver_ingot_from_ore");
    }

    private void generateShapedCrafting(RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AirwaveItems.CRYSTAL_OSCILLATOR)
                .pattern("SAS")
                .pattern(" C ")
                .define('S', AirwaveMaterials.SILVER.ingots)
                .define('A', AirwaveItems.CUT_AMETHYST)
                .define('C', AirwaveMaterials.COPPER.wire)
                .unlockedBy("has_silver_ingot", has(AirwaveMaterials.SILVER.ingots))
                .unlockedBy("has_cut_amethyst", has(AirwaveItems.CUT_AMETHYST))
                .unlockedBy("has_copper_wire", has(AirwaveMaterials.COPPER.wire))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AirwaveBlocks.SILVER_BLOCK)
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', AirwaveMaterials.SILVER.ingots)
                .unlockedBy("has_silver_ingot", has(AirwaveMaterials.SILVER.ingots))
                .save(out);
    }

    private void generateShapelessCrafting(RecipeOutput out) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AirwaveItems.SILVER_INGOT.asStack(9))
                .requires(AirwaveBlocks.SILVER_BLOCK)
                .unlockedBy("has_silver_block", has(AirwaveBlocks.SILVER_BLOCK))
                .save(out, "silver_ingot_from_silver_block");
    }
}
