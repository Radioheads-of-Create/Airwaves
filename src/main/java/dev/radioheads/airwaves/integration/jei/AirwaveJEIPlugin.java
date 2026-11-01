package dev.radioheads.airwaves.integration.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.content.recipe.FoilApplicationRecipe;
import dev.radioheads.airwaves.integration.jei.category.FoilApplicationCategory;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveItems;
import dev.radioheads.airwaves.registry.AirwaveRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@NothingNullByDefault
@SuppressWarnings("unused")
public class AirwaveJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = Airwaves.asResource("jei_plugin");

    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

    private void loadCategories() {
        this.allCategories.clear();
        CreateRecipeCategory<?>
                foilApplication = builder(FoilApplicationRecipe.class)
                .addTypedRecipes(AirwaveRecipeTypes.FOIL_APPLICATION)
                .catalyst(AirwaveBlocks.FOIL_APPLICATOR::get)
                .itemIcon(AirwaveItems.SILVER_FOIL)
                .emptyBackground(170, 150)
                .build("foil_application", FoilApplicationCategory::new);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(this.allCategories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        this.allCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        this.allCategories.forEach(c -> c.registerCatalysts(registration));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    private <T extends Recipe<? extends RecipeInput>> AirwaveJEIPlugin.CategoryBuilder<T> builder(Class<T> recipeClass) {
        return new AirwaveJEIPlugin.CategoryBuilder<>(recipeClass);
    }

    private class CategoryBuilder<T extends Recipe<?>> extends CreateRecipeCategory.Builder<T> {
        public CategoryBuilder(Class<? extends T> recipeClass) {
            super(recipeClass);
        }
        @Override
        public CreateRecipeCategory<T> build(ResourceLocation id, CreateRecipeCategory.Factory<T> factory) {
            CreateRecipeCategory<T> category = super.build(id, factory);
            allCategories.add(category);
            return category;
        }
    }
}
