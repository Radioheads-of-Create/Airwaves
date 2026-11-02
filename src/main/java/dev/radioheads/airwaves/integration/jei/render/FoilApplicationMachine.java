package dev.radioheads.airwaves.integration.jei.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwavePartialModels;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
//TODO
public class FoilApplicationMachine extends AnimatedKinetics {
    public void draw(GuiGraphics graphics, int xOffset, int yOffset, int foilColor, boolean shadow) {
        PoseStack matrixStack = graphics.pose();

        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 0.0);
        if (shadow)
            AllGuiTextures.JEI_SHADOW.render(graphics, -16, 13);
        matrixStack.translate(-2.0, 18.0, 0.0);
        int scale = 22;

        GuiGameElement.of(AirwaveBlocks.FOIL_APPLICATOR.getDefaultState().setValue(FoilApplicatorBlock.HORIZONTAL_FACING, Direction.NORTH))
                .rotateBlock(22.5, 22.5, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(shaft(Direction.Axis.Z))
                .rotateBlock(22.5, 22.5 + 270, getCurrentAngle())
                .scale(scale)
                .render(graphics);

        blockElement(AllPartialModels.BELT_END)
                .rotateBlock(22.5, 22.5, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(AirwavePartialModels.FOIL_ROLL)
                .rotateBlock(22.5, 22.5, getCurrentAngle())
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {

    }
}
