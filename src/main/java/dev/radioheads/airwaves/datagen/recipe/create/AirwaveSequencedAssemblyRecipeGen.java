package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.recipe.FoilApplicationRecipe;
import dev.radioheads.airwaves.data.AirwaveMaterials;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public final class AirwaveSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {
    public AirwaveSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Airwaves.MOD_ID);
    }

    GeneratedRecipe RESISTOR = create("resistor", b -> b
            .loops(1)
            .transitionTo(AirwaveItems.INCOMPLETE_RESISTOR)
            .require(AirwaveMaterials.SILVER.wire)
            .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_RESISTOR), Ingredient.of(ItemTags.COALS)))
            .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_RESISTOR), Ingredient.of(Items.CLAY_BALL)))
            .addOutput(AirwaveItems.RESISTOR, 0.9f)
    ),
        SPEAKER_CONE = create("speaker_cone", b -> b
                .loops(1)
                .transitionTo(AirwaveItems.INCOMPLETE_SPEAKER_CONE)
                .require(Items.PAPER)
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SPEAKER_CONE), Ingredient.of(Tags.Items.NUGGETS)))
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SPEAKER_CONE), Ingredient.of(AirwaveMaterials.COPPER.wire)))
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SPEAKER_CONE), Ingredient.of(AirwaveMaterials.SILVER.plates)))
                .addOutput(AirwaveItems.SPEAKER_CONE, 0.9f)
        ),

        TEST = create("test", b -> b
                .loops(3)
                .transitionTo(AirwaveItems.INCOMPLETE_SPEAKER_CONE)
                .require(AirwaveMaterials.SILVER.ingots)
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SPEAKER_CONE), Ingredient.of(Tags.Items.NUGGETS)))
                .addStep(FoilApplicationRecipe::builder, d -> d.foilUsage(50).duration(30).withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SPEAKER_CONE), Ingredient.of(AirwaveMaterials.GOLD.foil)))
                .addOutput(Items.DIORITE, 0.9f)

        ),

        SILVER_PRECISION_MECHANISM = create("silver_precision_mechanism", b -> b
            .loops(5)
            .transitionTo(AirwaveItems.INCOMPLETE_SILVER_PRECISION_MECHANISM)
                .require(AllItems.PRECISION_MECHANISM)
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SILVER_PRECISION_MECHANISM), Ingredient.of(AirwaveMaterials.SILVER.spool)))
            .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveItems.INCOMPLETE_SILVER_PRECISION_MECHANISM), Ingredient.of(AllItems.ELECTRON_TUBE)))
            .addOutput(AirwaveItems.SILVER_PRECISION_MECHANISM, 0.7f)
        );


}
