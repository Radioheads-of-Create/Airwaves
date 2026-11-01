package dev.radioheads.airwaves.content.recipe;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import dev.radioheads.airwaves.AirwaveLang;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.integration.jei.category.FoilApplicationCategory;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveRecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@NothingNullByDefault
public class FoilApplicationRecipe extends StandardProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {
    public FoilApplicationRecipe(ProcessingRecipeParams params) {
        super(AirwaveRecipeTypes.FOIL_APPLICATION, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(RecipeWrapper wrapper, Level level) {
        return !wrapper.isEmpty() && this.ingredients.getFirst().test(wrapper.getItem(0));
    }

    @Override
    public Component getDescriptionForAssembly() {
        ItemStack[] matchingStacks = getIngredients().getFirst().getItems();
        if (matchingStacks.length == 0) {
            return Component.literal("Invalid");
        }
        return AirwaveLang.translate("recipe.assembly.foil_applicator", Component.translatable(matchingStacks[0].getDescriptionId()).getString()).component();
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(AirwaveBlocks.FOIL_APPLICATOR);
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(this.ingredients.get(1));
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> FoilApplicationCategory.AssemblyFoilApplication::new;
    }
}
