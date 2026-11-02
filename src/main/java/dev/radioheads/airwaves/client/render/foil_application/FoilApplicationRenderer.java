package dev.radioheads.airwaves.client.render.foil_application;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class FoilApplicationRenderer extends KineticBlockEntityRenderer<FoilApplicatorBlockEntity> {
    public FoilApplicationRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FoilApplicatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(FoilApplicatorBlockEntity be, BlockState state) {
        Direction shaftDir = state.getValue(FoilApplicatorBlock.HORIZONTAL_FACING)
                .getCounterClockWise();
        return CachedBuffers.partial(AllPartialModels.SHAFT, state)
                .rotateToFace(shaftDir);
    }
}
