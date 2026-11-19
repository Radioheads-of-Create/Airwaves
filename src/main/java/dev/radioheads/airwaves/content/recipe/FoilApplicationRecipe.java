package dev.radioheads.airwaves.content.recipe;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import dev.radioheads.airwaves.AirwaveLang;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.recipe.foil.FoilApplicationRecipeParams;
import dev.radioheads.airwaves.integration.jei.category.FoilApplicationCategory;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@NothingNullByDefault
public class FoilApplicationRecipe extends ProcessingRecipe<RecipeWrapper, FoilApplicationRecipeParams> implements IAssemblyRecipe {
    protected int foilUsage;

    public FoilApplicationRecipe(FoilApplicationRecipeParams params) {
        super(AirwaveRecipeTypes.FOIL_APPLICATION, params);
        this.foilUsage = params.foilUsage;
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

    public int getFoilUsage() {
        return foilUsage;
    }

    @Override
    public boolean matches(RecipeWrapper wrapper, Level level) {
        return ingredients.get(0).test(wrapper.getItem(0)) && ingredients.get(1).test(wrapper.getItem(1));
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

    @Override
    public List<String> validate() {
        if (foilUsage < 1) super.validate().add("Recipe must have foil usage 1 or greater, currently (" + foilUsage + ")");
        return super.validate();
    }

    @FunctionalInterface
    public interface Factory<R extends FoilApplicationRecipe> extends ProcessingRecipe.Factory<FoilApplicationRecipeParams, R> {
        R create(FoilApplicationRecipeParams params);
    }

    public static class Builder<R extends FoilApplicationRecipe> extends ProcessingRecipeBuilder<FoilApplicationRecipeParams, R, FoilApplicationRecipe.Builder<R>> {
        public Builder(FoilApplicationRecipe.Factory<R> factory, ResourceLocation recipeId) {
            super(factory, recipeId);
        }

        @Override
        protected FoilApplicationRecipeParams createParams() {
            return new FoilApplicationRecipeParams();
        }

        @Override
        public Builder<R> self() {
            return this;
        }

        public FoilApplicationRecipe.Builder<R> foilUsage(int usage) {
            if (usage < 1) {
                throw new IllegalArgumentException("Foil Usage can not be less than 1");
            }
            params.foilUsage = usage;
            return this;
        }
    }


    public static class Serializer<R extends FoilApplicationRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ProcessingRecipe.Factory<FoilApplicationRecipeParams, R> factory) {
            this.codec = ProcessingRecipe.codec(factory, FoilApplicationRecipeParams.CODEC);
            this.streamCodec = ProcessingRecipe.streamCodec(factory, FoilApplicationRecipeParams.STREAM_CODEC);
        }

        @Override
        public MapCodec<R> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }
    }
}
