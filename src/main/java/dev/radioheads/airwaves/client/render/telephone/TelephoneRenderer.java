package dev.radioheads.airwaves.client.render.telephone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.radioheads.airwaves.client.cable.CableRenderer;
import dev.radioheads.airwaves.content.block.HfTransceiverPanel;
import dev.radioheads.airwaves.content.block.TelephoneBlock;
import dev.radioheads.airwaves.content.entity.block.TelephoneBlockEntity;
import dev.radioheads.airwaves.content.telephone.EarpieceBinding;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import dev.radioheads.airwaves.registry.AirwavePartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TelephoneRenderer extends SafeBlockEntityRenderer<TelephoneBlockEntity> {
    // Button travel to the dial top
    private static final float PRESS_PIXELS = 0.07984F;
    private static final double CORD_SEARCH = 12.0;
    private static final float CORD_WIDTH = 0.025F;
    private static final int CORD_COLOR = 0x1C1A18;
    // First person earpiece offset
    private static final float FIRST_PERSON_ACROSS = 0.72F;
    private static final float FIRST_PERSON_DOWN = -0.6F;

    public TelephoneRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(TelephoneBlockEntity phone, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = phone.getBlockState();
        if (!(state.getBlock() instanceof TelephoneBlock)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-HfTransceiverPanel.modelRotation(state.getValue(TelephoneBlock.FACING))));
        poseStack.translate(-0.5, 0.0, -0.5);
        for (int button = 0; button < AirwavePartialModels.TELEPHONE_BUTTONS.length; button++) {
            float depth = phone.pressDepth(button, partialTicks) * PRESS_PIXELS / 16.0F;
            poseStack.pushPose();
            poseStack.translate(0.0, -depth, 0.0);
            CachedBuffers.partial(AirwavePartialModels.TELEPHONE_BUTTONS[button], state).light(light).renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
            poseStack.popPose();
        }
        poseStack.popPose();

        cord(phone, partialTicks, poseStack, buffer);
        CableRenderer.renderCables(phone, poseStack, buffer);
    }

    // Earpiece cord
    private static void cord(TelephoneBlockEntity phone, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        UUID earpiece = phone.earpiece();
        Level level = phone.getLevel();
        if (earpiece == null || level == null || phone.hooked()) {
            return;
        }
        Vec3 jack = phone.anchor();
        Vec3 end = holderHand(level, jack, earpiece, partialTicks);
        if (end == null) {
            end = droppedEarpiece(level, jack, earpiece, partialTicks);
        }
        if (end == null) {
            return;
        }
        BlockPos origin = phone.getBlockPos();
        CableRenderer.cord(poseStack, buffer, jack.subtract(Vec3.atLowerCornerOf(origin)), end.subtract(Vec3.atLowerCornerOf(origin)),
                LevelRenderer.getLightColor(level, BlockPos.containing(jack)), LevelRenderer.getLightColor(level, BlockPos.containing(end)),
                CORD_WIDTH, CORD_COLOR);
    }

    // Cord end
    @Nullable
    private static Vec3 holderHand(Level level, Vec3 jack, UUID earpiece, float partialTicks) {
        for (Player player : level.players()) {
            if (player.position().closerThan(jack, CORD_SEARCH)) {
                for (InteractionHand hand : InteractionHand.values()) {
                    if (holds(player.getItemInHand(hand), earpiece)) {
                        Vec3 cap = EarpieceCord.cap(earpiece);
                        return cap != null ? cap : handPosition(player, hand, partialTicks);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static Vec3 droppedEarpiece(Level level, Vec3 jack, UUID earpiece, float partialTicks) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(jack, jack).inflate(CORD_SEARCH))) {
            if (holds(item.getItem(), earpiece)) {
                Vec3 cap = EarpieceCord.cap(earpiece);
                return cap != null ? cap : item.getPosition(partialTicks).add(0.0, 0.6, 0.0);
            }
        }
        return null;
    }

    private static boolean holds(ItemStack stack, UUID earpiece) {
        EarpieceBinding binding = stack.get(AirwaveDataComponents.EARPIECE);
        return binding != null && binding.earpiece().equals(earpiece);
    }

    // Hand position like a fishing line
    private static Vec3 handPosition(Player player, InteractionHand hand, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            double fov = 960.0 / minecraft.options.fov().get();
            Vec3 offset = minecraft.gameRenderer.getMainCamera().getNearPlane().getPointOnPlane(side * FIRST_PERSON_ACROSS, FIRST_PERSON_DOWN).scale(fov);
            return player.getEyePosition(partialTicks).add(offset);
        }
        float body = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(body);
        double cos = Mth.cos(body);
        float scale = player.getScale();
        double across = side * 0.35 * scale;
        double forward = 0.8 * scale;
        double crouch = player.isCrouching() ? -0.1875 : 0.0;
        return player.getEyePosition(partialTicks).add(-cos * across - sin * forward, crouch - 0.45 * scale, -sin * across + cos * forward);
    }

    @Override
    public boolean shouldRenderOffScreen(TelephoneBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return CableRenderer.VIEW_DISTANCE;
    }

    @Override
    public AABB getRenderBoundingBox(TelephoneBlockEntity blockEntity) {
        return CableRenderer.bounds(blockEntity).minmax(new AABB(blockEntity.getBlockPos()).inflate(CORD_SEARCH));
    }
}
