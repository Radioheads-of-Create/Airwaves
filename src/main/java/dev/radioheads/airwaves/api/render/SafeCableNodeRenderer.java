package dev.radioheads.airwaves.api.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.client.cable.CableRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

@NothingNullByDefault
public abstract class SafeCableNodeRenderer<T extends BlockEntity> extends SafeBlockEntityRenderer<T> {
    public final void renderCables(T blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        CableRenderer.renderCables(blockEntity, poseStack, bufferSource);
    }

    @Override
    public int getViewDistance() {
        return CableRenderer.VIEW_DISTANCE;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return CableRenderer.bounds(blockEntity);
    }
}
