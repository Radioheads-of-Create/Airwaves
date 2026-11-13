package dev.radioheads.airwaves.client.render.foil_application;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwavePartialModels;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class FoilApplicationRenderer extends KineticBlockEntityRenderer<FoilApplicatorBlockEntity> {
    public FoilApplicationRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull FoilApplicatorBlockEntity be) {
        return true;
    }

    @Override
    protected void renderSafe(FoilApplicatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        Direction.Axis axis = getRotationAxisOf(be);
        BlockPos pos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(FoilApplicatorBlock.HORIZONTAL_FACING);

        SuperByteBuffer rollRender = CachedBuffers.partialFacing(AirwavePartialModels.FOIL_ROLL, blockState, facing);
        SuperByteBuffer foilBlock = CachedBuffers.partialFacing(AirwavePartialModels.FOIL_ROLL_BLOCK, blockState, facing);

        Vector3f offset = switch (blockState.getValue(FoilApplicatorBlock.HORIZONTAL_FACING)) {
            case DOWN, UP -> new Vector3f();
            case NORTH -> new Vector3f(0, 10/16f + 0.1f, -7/16f);
            case SOUTH -> new Vector3f(0, 10/16f + 0.1f, 7/16f);
            case WEST -> new Vector3f(-7/16f, 10/16f + 0.1f, 0);
            case EAST -> new Vector3f(7/16f, 10/16f + 0.1f, 0);
        };

        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float angleOffset = getRotationOffsetForPosition(be, pos, axis);
        float angle = ((time * (be.getSpeed() * -1) * 3f / 10 + angleOffset) % 360) / 180 * (float) Math.PI;

        rollRender.translate(offset.x(), offset.y(), offset.z());
        foilBlock.translate(offset.x(), offset.y(), offset.z());

        kineticRotationTransform(rollRender, be, axis, angle, light);
        kineticRotationTransform(foilBlock, be, axis, angle, light);

        rollRender.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        foilBlock.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected BlockState getRenderedBlockState(FoilApplicatorBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
