package dev.radioheads.airwaves.integration.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
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

    }

    @Override
    protected void draw(FoilApplicationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        machine.draw(gui, 50, 50, 0, true);
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
