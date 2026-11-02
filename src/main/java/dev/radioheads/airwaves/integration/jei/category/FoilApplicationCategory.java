package dev.radioheads.airwaves.integration.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.content.recipe.FoilApplicationRecipe;
import dev.radioheads.airwaves.integration.jei.render.FoilApplicationMachine;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;

@NothingNullByDefault
//TODO
public class FoilApplicationCategory extends CreateRecipeCategory<FoilApplicationRecipe> {
    private final FoilApplicationMachine machine = new FoilApplicationMachine();

    public FoilApplicationCategory(Info<FoilApplicationRecipe> info) {
        super(info);
    }

    @Override
    protected void setRecipe(IRecipeLayoutBuilder builder, FoilApplicationRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(35, 6).addIngredients(recipe.getIngredients().getFirst());
        builder.addInputSlot(90, 6).addIngredients(recipe.getIngredients().get(1));
        builder.addOutputSlot(96, 46).addItemStack(recipe.getResultItem(null));
    }

    @Override
    protected void draw(FoilApplicationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        machine.draw(gui, 65, 26, 0, true);
        AllGuiTextures.JEI_DOWN_ARROW.render(gui, 90, 28);
        AllGuiTextures.JEI_SLOT.render(gui, 34, 6);
        AllGuiTextures.JEI_SLOT.render(gui, 90, 6);
        AllGuiTextures.JEI_SLOT.render(gui, 95, 46);
    }

    public static class AssemblyFoilApplication extends SequencedAssemblySubCategory {
        public AssemblyFoilApplication() {
            super(25);
        }

        @Override
        public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {

        }
    }
}
