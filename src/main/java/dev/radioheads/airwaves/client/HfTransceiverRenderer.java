package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.registry.AirwavePartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.content.block.HfTransceiverControl;
import dev.radioheads.airwaves.content.block.HfTransceiverPanel;
import dev.radioheads.airwaves.content.entity.block.HfTransceiverBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public class HfTransceiverRenderer extends SafeBlockEntityRenderer<HfTransceiverBlockEntity> {
    private static final float KEY_TRAVEL = 0.12F / 16.0F;
    private static final float EASE = 0.35F;
    private static final float SCREEN_LIFT = 0.01F;
    private final Map<HfTransceiverBlockEntity, float[]> angles = new WeakHashMap<>();

    public HfTransceiverRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(HfTransceiverBlockEntity transceiver, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = transceiver.getBlockState();
        if (!(state.getBlock() instanceof HfTransceiverBlock) || transceiver.getLevel() == null) {
            return;
        }
        HfTransceiverBlock.Finish finish = state.getValue(HfTransceiverBlock.FINISH);
        long now = transceiver.getLevel().getGameTime();
        VertexConsumer cutout = buffer.getBuffer(RenderType.cutout());
        float[] shown = this.angles.computeIfAbsent(transceiver, key -> new float[] {
                angle(key, HfTransceiverControl.VFO), angle(key, HfTransceiverControl.SQL), angle(key, HfTransceiverControl.AF)});

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-HfTransceiverPanel.modelRotation(state.getValue(HfTransceiverBlock.HORIZONTAL_FACING))));
        poseStack.translate(-0.5, 0.0, -0.5);

        int knob = 0;
        for (HfTransceiverControl control : HfTransceiverControl.values()) {
            poseStack.pushPose();
            if (control.kind == HfTransceiverControl.Kind.KEY) {
                if (transceiver.pressedRecently(control, now)) {
                    poseStack.translate(0.0, 0.0, KEY_TRAVEL);
                }
            } else {
                int index = knob++;
                shown[index] += (angle(transceiver, control) - shown[index]) * EASE;
                float[] pivot = control.pivot;
                poseStack.translate(pivot[0] / 16.0F, pivot[1] / 16.0F, pivot[2] / 16.0F);
                poseStack.mulPose(control.axis == 'x' ? Axis.XP.rotationDegrees(shown[index]) : Axis.ZP.rotationDegrees(shown[index]));
                poseStack.translate(-pivot[0] / 16.0F, -pivot[1] / 16.0F, -pivot[2] / 16.0F);
            }
            CachedBuffers.partial(AirwavePartialModels.hfTransceiver(finish, control.id()), state).light(light).renderInto(poseStack, cutout);
            poseStack.popPose();
        }

        boolean onAir = state.getValue(HfTransceiverBlock.ON_AIR);
        led(poseStack, cutout, state, finish, "on_air", onAir, light);
        led(poseStack, cutout, state, finish, "rx", !onAir && transceiver.receiving(), light);
        screen(transceiver, poseStack, buffer, finish, onAir, now, light);
        poseStack.popPose();

        CableRenderer.renderCables(transceiver, poseStack, buffer);
    }

    private static float angle(HfTransceiverBlockEntity transceiver, HfTransceiverControl control) {
        return switch (control) {
            case VFO -> transceiver.tuningSteps() * 3.6F;
            case SQL -> transceiver.knobSquelch() * 270.0F;
            case AF -> transceiver.knobVolume() * 540.0F;
            default -> 0.0F;
        };
    }

    private static void led(PoseStack poseStack, VertexConsumer consumer, BlockState state, HfTransceiverBlock.Finish finish, String name, boolean lit, int light) {
        CachedBuffers.partial(AirwavePartialModels.hfTransceiver(finish, lit ? name + "_lit" : name), state)
                .light(lit ? LightTexture.FULL_BRIGHT : light)
                .renderInto(poseStack, consumer);
    }

    private static void screen(HfTransceiverBlockEntity transceiver, PoseStack poseStack, MultiBufferSource buffer, HfTransceiverBlock.Finish finish,
                               boolean onAir, long now, int light) {
        AntennaProfile antenna = transceiver.antennaProfile();
        String antennaText = transceiver.antennaShown(now) ? switch (antenna.shape()) {
            case NONE -> "NONE";
            case WHIP -> "WHIP";
            case DIPOLE -> "DIPOLE";
            case YAGI -> "YAGI " + antenna.elements();
        } : null;
        HfTransceiverDisplay.Frame frame = new HfTransceiverDisplay.Frame(
                !transceiver.standby(), onAir, transceiver.slotB(), transceiver.locked(), transceiver.fast(), transceiver.megahertz(),
                Math.round(transceiver.signal() * 15.0F), transceiver.receiving(), transceiver.hertz(), transceiver.displayWatts(),
                transceiver.powerShown(now) && now / 3 % 2 == 0,
                antennaText, antennaText == null ? null : String.format(Locale.ROOT, "-%.1fDB", transceiver.feedLossDb()),
                transceiver.volumeShown(now) ? Math.round(transceiver.knobVolume() * 100.0F) : -1,
                finish == HfTransceiverBlock.Finish.BLACK ? HfTransceiverDisplay.AMBER : HfTransceiverDisplay.GREEN);
        ResourceLocation texture = HfTransceiverScreens.show(transceiver.getBlockPos(), frame);

        float[] s = HfTransceiverControl.SCREEN;
        float x0 = s[0] / 16.0F, y0 = s[1] / 16.0F, x1 = s[3] / 16.0F, y1 = s[4] / 16.0F, z = (s[2] - SCREEN_LIFT) / 16.0F;
        int screenLight = transceiver.standby() ? light : LightTexture.FULL_BRIGHT;
        VertexConsumer consumer = buffer.getBuffer(RenderType.text(texture));
        Matrix4f pose = poseStack.last().pose();
        consumer.addVertex(pose, x1, y1, z).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setLight(screenLight);
        consumer.addVertex(pose, x1, y0, z).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setLight(screenLight);
        consumer.addVertex(pose, x0, y0, z).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setLight(screenLight);
        consumer.addVertex(pose, x0, y1, z).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setLight(screenLight);
    }

    @Override
    public boolean shouldRenderOffScreen(HfTransceiverBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return CableRenderer.VIEW_DISTANCE;
    }

    @Override
    public AABB getRenderBoundingBox(HfTransceiverBlockEntity blockEntity) {
        return CableRenderer.bounds(blockEntity);
    }
}
