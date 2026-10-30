package dev.radioheads.airwaves.datagen.recipe.integration.createoreexcavation;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import net.minecraft.data.recipes.RecipeOutput;

@NothingNullByDefault
//TODO: get the COE fork completed and update this
public final class AirwaveCOEDrillingRecipeGen {
    private AirwaveCOEDrillingRecipeGen() {}

    public static void buildRecipes(RecipeOutput out) {
        // var conditionalOut = out.withConditions(IntegratedMods.CREATE_ORE_EXCAVATION.getCondition());

        // COE... why...
        // There are... far better ways... to have this...
        // I mean, it works, but why
//        new COERecipes.DrillingBuilder(AirwaveItems.RAW_SILVER.get(), 40 * 20, 1536, 512)
//                .setBiomeWhitelist(BiomeTags.IS_OVERWORLD)
//                .veinSize(2, 6)
//                .setStress(320)
//                .save("silver", conditionalOut);
    }
}
