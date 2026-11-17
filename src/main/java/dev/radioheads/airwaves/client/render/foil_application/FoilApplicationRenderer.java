package dev.radioheads.airwaves.client.render.foil_application;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.api.util.AirwaveUtil;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class FoilApplicationRenderer extends SafeBlockEntityRenderer<FoilApplicatorBlockEntity> {
    public FoilApplicationRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen(@NotNull FoilApplicatorBlockEntity be) {
        return true;
    }

    @Override
    protected void renderSafe(FoilApplicatorBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(FoilApplicatorBlock.HORIZONTAL_FACING);

        Vec3 offset = FoilApplicatorBlock.rollOffset(facing);
        float ox = (float) offset.x, oy = (float) offset.y, oz = (float) offset.z;

        Direction rollAxis = facing.getClockWise();
        float angle = be.getRollAngle(partialTicks) / 180f * (float) Math.PI;

        SuperByteBuffer roll = CachedBuffers.partialFacing(AirwavePartialModels.FOIL_ROLL, blockState, facing);
        roll.translate(ox, oy, oz)
                .rotateCentered(angle, rollAxis)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        FoilType type = be.getFoilType();
        if (type != null) {
            SuperByteBuffer foil = CachedBuffers.partialFacing(AirwavePartialModels.FOIL_ROLL_BLOCK, blockState, facing);
            foil.translate(ox, oy, oz)
                    .rotateCentered(angle, rollAxis)
                    .light(light)
                    .color(type.color())
                    .renderInto(ms, buffer.getBuffer(RenderType.SOLID).setColor(type.color()));
        }
    }
}
