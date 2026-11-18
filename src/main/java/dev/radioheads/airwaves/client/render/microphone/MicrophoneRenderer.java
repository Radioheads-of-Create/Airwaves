package dev.radioheads.airwaves.client.render.microphone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.radioheads.airwaves.api.render.SafeCableNodeRenderer;
import dev.radioheads.airwaves.content.block.HfTransceiverPanel;
import dev.radioheads.airwaves.content.block.MicrophoneBlock;
import dev.radioheads.airwaves.content.block.TurtleMicBlock;
import dev.radioheads.airwaves.content.entity.block.MicrophoneBlockEntity;
import dev.radioheads.airwaves.registry.AirwavePartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

public class MicrophoneRenderer extends SafeCableNodeRenderer<MicrophoneBlockEntity> {
    private static final float EASE = 0.35F;
    private final Map<MicrophoneBlockEntity, float[]> angles = new WeakHashMap<>();

    public MicrophoneRenderer(BlockEntityRendererProvider.Context context) {}

    // One full turn from 100 percent down to 0
    private static float angle(MicrophoneBlockEntity microphone) {
        return -(1.0F - microphone.gain()) * 360.0F;
    }

    @Override
    protected void renderSafe(MicrophoneBlockEntity microphone, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = microphone.getBlockState();
        if (!(state.getBlock() instanceof MicrophoneBlock || state.getBlock() instanceof TurtleMicBlock)) {
            return;
        }
        float[] shown = this.angles.computeIfAbsent(microphone, key -> new float[]{angle(key)});
        shown[0] += (angle(microphone) - shown[0]) * EASE;

        Vec3 knob = state.getBlock() instanceof MicrophoneBlock ? MicrophoneBlock.KNOB.scale(1.0 / 16.0) : TurtleMicBlock.KNOB.scale(1.0 / 16.0);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-HfTransceiverPanel.modelRotation(state.getValue(state.getBlock() instanceof MicrophoneBlock ? MicrophoneBlock.FACING : TurtleMicBlock.FACING))));
        poseStack.translate(knob.x - 0.5, knob.y, knob.z - 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(shown[0]));
        poseStack.translate(-knob.x, -knob.y, -knob.z);
        CachedBuffers.partial(AirwavePartialModels.MICROPHONE_KNOB, state).light(light).renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
        poseStack.popPose();

        renderCables(microphone, poseStack, buffer);
    }
}
