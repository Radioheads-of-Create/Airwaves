package dev.radioheads.airwaves.client.render.telephone;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.telephone.EarpieceBinding;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Earpiece cap positions this frame
@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class EarpieceCord {
    // Cap tip in item model pixels
    private static final Vector3f CAP = new Vector3f(8.25F, 9.4F, 8.0F).div(16.0F).sub(0.5F, 0.5F, 0.5F);
    private static final Map<UUID, Drawn> CAPS = new HashMap<>();

    private static long frame;
    private static double worldFov = 70.0;
    private static double handFov = 70.0;

    // Camera relative offset
    private record Drawn(Vector3f at, boolean view, long frame) {}

    private EarpieceCord() {}

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Pre event) {
        frame++;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov()) {
            worldFov = event.getFOV();
        } else {
            handFov = event.getFOV();
        }
    }

    @SubscribeEvent
    public static void onBake(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation key = ModelResourceLocation.inventory(Airwaves.asResource("telephone_earpiece"));
        BakedModel model = event.getModels().get(key);
        if (model != null) {
            event.getModels().put(key, new Unbound(model));
        }
    }

    private static void drawn(UUID earpiece, ItemDisplayContext context, PoseStack poseStack) {
        boolean held = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (context.firstPerson() || held || context == ItemDisplayContext.GROUND) {
            CAPS.put(earpiece, new Drawn(poseStack.last().pose().transformPosition(new Vector3f(CAP)), context.firstPerson(), frame));
        }
    }

    // Last drawn cap position
    @Nullable
    public static Vec3 cap(UUID earpiece) {
        Drawn drawn = CAPS.get(earpiece);
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (drawn == null) {
            return null;
        }
        if (!drawn.view()) {
            return drawn.frame() == frame ? camera.getPosition().add(drawn.at().x, drawn.at().y, drawn.at().z) : null;
        }
        if (frame - drawn.frame() > 1 || !minecraft.options.getCameraType().isFirstPerson()) {
            return null;
        }
        // Hand FOV to world FOV
        float widen = (float) (Math.tan(Math.toRadians(worldFov) / 2.0) / Math.tan(Math.toRadians(handFov) / 2.0));
        Vector3f view = camera.rotation().conjugate(new Quaternionf()).transform(new Vector3f(drawn.at()));
        view.mul(widen, widen, 1.0F);
        camera.rotation().transform(view);
        return camera.getPosition().add(view.x, view.y, view.z);
    }

    private static final class Unbound extends BakedModelWrapper<BakedModel> {
        private final ItemOverrides overrides;

        private Unbound(BakedModel original) {
            super(original);
            this.overrides = new Overrides(original);
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.overrides;
        }
    }

    // Per earpiece render wrapper
    private static final class Overrides extends ItemOverrides {
        private final BakedModel original;

        private Overrides(BakedModel original) {
            this.original = original;
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            BakedModel resolved = this.original.getOverrides().resolve(this.original, stack, level, entity, seed);
            EarpieceBinding binding = stack.get(AirwaveDataComponents.EARPIECE);
            return binding == null || resolved == null ? resolved : new Bound(resolved, binding.earpiece());
        }
    }

    private static final class Bound extends BakedModelWrapper<BakedModel> {
        private final UUID earpiece;

        private Bound(BakedModel original, UUID earpiece) {
            super(original);
            this.earpiece = earpiece;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
            BakedModel model = super.applyTransform(context, poseStack, leftHand);
            drawn(this.earpiece, context, poseStack);
            return model;
        }
    }
}
