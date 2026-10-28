package dev.radioheads.airwaves.datagen.recipe.create;

import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import dev.radioheads.airwaves.Airwaves;
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
            .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(ItemTags.COALS)))
            .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(Items.CLAY_BALL)))
            .addOutput(AirwaveItems.RESISTOR, 0.9f)
    ),
        SPEAKER_CONE = create("speaker_cone", b -> b
                .loops(1)
                .transitionTo(AirwaveItems.INCOMPLETE_SPEAKER_CONE)
                .require(Items.PAPER)
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(Tags.Items.NUGGETS)))
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveMaterials.COPPER.wire)))
                .addStep(DeployerApplicationRecipe::new, d -> d.withItemIngredients(Ingredient.of(AirwaveMaterials.SILVER.plates)))
                .addOutput(AirwaveItems.SPEAKER_CONE, 0.9f)
        )
    ;
}
