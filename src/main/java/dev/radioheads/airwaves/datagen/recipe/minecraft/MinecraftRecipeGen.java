package dev.radioheads.airwaves.datagen.recipe.minecraft;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

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
        generateSpecialCrafting(out);
    }

    private void generateCookingRecipes(RecipeOutput out) {
        SimpleCookingRecipeBuilder.blasting(
                Ingredient.of(AirwaveMaterials.SILVER.rawMaterial),
                RecipeCategory.MISC,
                AirwaveItems.SILVER_INGOT.asStack(9),
                0.1f,
                200
        )
                .unlockedBy("has_raw_silver", has(AirwaveMaterials.SILVER.rawMaterial))
                .save(out, "silver_blasting");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(AirwaveMaterials.SILVER.rawMaterial),
                        RecipeCategory.MISC,
                        AirwaveItems.SILVER_INGOT.asStack(9),
                        0.1f,
                        200
                )
                .unlockedBy("has_raw_silver", has(AirwaveMaterials.SILVER.rawMaterial))
                .save(out, "silver_smelting");


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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AirwaveBlocks.COPPER_ANTENNA_ROD)
                .pattern(" S ")
                .pattern("SIS")
                .pattern(" S ")
                .define('S', AirwaveMaterials.COPPER.plates)
                .define('I', AirwaveMaterials.COPPER.ingots)
                .unlockedBy("has_copper_ingot", has(AirwaveMaterials.COPPER.ingots))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AirwaveBlocks.SILVER_ANTENNA_ROD)
                .pattern(" S ")
                .pattern("SIS")
                .pattern(" S ")
                .define('S', AirwaveMaterials.SILVER.plates)
                .define('I', AirwaveMaterials.SILVER.ingots)
                .unlockedBy("has_silver_ingot", has(AirwaveMaterials.COPPER.ingots))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AirwaveBlocks.GOLD_ANTENNA_ROD)
                .pattern(" S ")
                .pattern("SIS")
                .pattern(" S ")
                .define('S', AirwaveMaterials.GOLD.plates)
                .define('I', AirwaveMaterials.GOLD.ingots)
                .unlockedBy("has_gold_ingot", has(AirwaveMaterials.GOLD.ingots))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AirwaveItems.MOSS_FOIL)
                .pattern(" M ")
                .pattern("MEM")
                .pattern(" M ")
                .define('M', Items.MOSS_CARPET)
                .define('E', AirwaveItems.EMPTY_SPOOL)
                .unlockedBy("has_empty_spool", has(AirwaveItems.EMPTY_SPOOL))
                .save(out);

        createFoil(AirwaveItems.COPPER_FOIL, AirwaveMaterials.COPPER, out);
        createFoil(AirwaveItems.GOLD_FOIL, AirwaveMaterials.GOLD, out);
        createFoil(AirwaveItems.SILVER_FOIL, AirwaveMaterials.SILVER, out);

        createSpool(AirwaveItems.COPPER_SPOOL, AirwaveMaterials.COPPER, out);
        createSpool(AirwaveItems.GOLD_SPOOL, AirwaveMaterials.GOLD, out);
        createSpool(AirwaveItems.SILVER_SPOOL, AirwaveMaterials.SILVER, out);
    }

    private static void createFoil(ItemLike foil, AirwaveMaterials material, RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, foil)
                .pattern(" M ")
                .pattern("MSM")
                .pattern(" M ")
                .define('M', material.plates)
                .define('S', AirwaveItems.EMPTY_SPOOL)
                .unlockedBy("has_spool", has(AirwaveItems.EMPTY_SPOOL))
                .unlockedBy("has_required_material", has(material.plates))
                .save(out);
    }

    private static void createSpool(ItemLike spool, AirwaveMaterials material, RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, spool)
                .pattern(" W ")
                .pattern("WSW")
                .pattern(" W ")
                .define('W', material.wire)
                .define('S', AirwaveItems.EMPTY_SPOOL)
                .unlockedBy("has_spool", has(AirwaveItems.EMPTY_SPOOL))
                .unlockedBy("has_required_material", has(material.wire))
                .save(out);
    }

    private void generateShapelessCrafting(RecipeOutput out) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AirwaveItems.SILVER_INGOT.asStack(9))
                .requires(AirwaveBlocks.SILVER_BLOCK)
                .unlockedBy("has_silver_block", has(AirwaveBlocks.SILVER_BLOCK))
                .save(out, "silver_ingot_from_silver_block");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AirwaveItems.FIELD_MANUAL)
                .requires(Items.BOOK)
                .requires(AirwaveMaterials.COPPER.wire)
                .unlockedBy("has_copper_wire", has(AirwaveMaterials.COPPER.wire))
                .save(out);
    }

    private void generateSpecialCrafting(RecipeOutput out) {
        SpecialRecipeBuilder.special(ArmorDyeRecipe::new)
                .save(out, "led_dyeing");
    }
}
