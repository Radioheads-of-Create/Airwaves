package dev.radioheads.airwaves.client.cable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.CableAim;
import dev.radioheads.airwaves.api.cable.CableLink;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.config.AirwaveConfig;
import dev.radioheads.airwaves.integration.sable.blocks.CableNodeSable;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class CableRenderer<T extends BlockEntity & CableNode> implements BlockEntityRenderer<T> {
    public static final int VIEW_DISTANCE = 96;
    private static final ResourceLocation TEXTURE = Airwaves.asResource("textures/misc/cable.png");
    private static final int COLOR = 0x242628;
    private static final float WIDTH = 0.06F;

    private static final float DEBUG_BOX_SIZE = 0.15F;
    private static final int DEBUG_SELF_COLOR = 0xFF0000;
    private static final int DEBUG_LINK_COLOR = 0x00FF00;

    public CableRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void renderCables(BlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        Level level = blockEntity.getLevel();
        if (level == null || !(blockEntity instanceof CableNode node)) {
            return;
        }

        BlockPos origin = blockEntity.getBlockPos();
        Vec3 renderOrigin = Vec3.atLowerCornerOf(origin);
        Vec3 from = node.anchor();

        if (AirwaveConfig.client().devConfig.nodeDebugRendering()) {
            renderDebugBox(level, from, renderOrigin, poseStack, buffer, DEBUG_BOX_SIZE, DEBUG_SELF_COLOR);
        }

        for (CableLink link : node.links().all()) {
            CableNode other = Cables.node(level, link.other());
            if (other == null || (link.other().asLong() < origin.asLong() && other.links().has(origin))) {
                continue;
            }

            Vec3 to = CableNodeSable.toLocal(level, node, other);

            if (AirwaveConfig.client().devConfig.nodeDebugRendering()) {
                renderDebugBox(level, to, renderOrigin, poseStack, buffer, DEBUG_BOX_SIZE, DEBUG_LINK_COLOR);
            }

            line(poseStack, buffer,
                    from.subtract(renderOrigin),
                    to.subtract(renderOrigin),
                    LevelRenderer.getLightColor(level, BlockPos.containing(from)),
                    LevelRenderer.getLightColor(level, BlockPos.containing(to)));
        }
    }

    public static Vec3 along(Vec3 from, Vec3 delta, double t) {
        return CableAim.along(from, delta, t);
    }

    private static void line(PoseStack poseStack, MultiBufferSource buffer, Vec3 from, Vec3 to, int lightFrom, int lightTo) {
        strand(poseStack, buffer, from, to, lightFrom, lightTo, WIDTH, COLOR);
    }

    // Telephone cord
    public static void cord(PoseStack poseStack, MultiBufferSource buffer, Vec3 from, Vec3 to, int lightFrom, int lightTo, float width, int color) {
        strand(poseStack, buffer, from, to, lightFrom, lightTo, width, color);
    }

    private static void strand(PoseStack poseStack, MultiBufferSource buffer, Vec3 from, Vec3 to, int lightFrom, int lightTo, float width, int color) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 0.001) {
            return;
        }

        int segments = Math.clamp((int) (length * 1.25), 4, 24);
        double horizontal = Math.hypot(delta.x, delta.z);
        Vec3 side = horizontal < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(-delta.z / horizontal, 0.0, delta.x / horizontal);
        Vec3 up   = horizontal < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);

        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        Vec3 previous = from;
        for (int i = 1; i <= segments; i++) {
            double t = (double) i / segments;
            Vec3 point = along(from, delta, t);
            Vec3 segment = point.subtract(previous);
            Vec3 vertical = up.subtract(segment.scale(up.dot(segment) / Math.max(1.0E-9, segment.lengthSqr()))).normalize().scale(width / 2.0F);
            Vec3 lateral = side.scale(width / 2.0F);
            int lightStart = mix(lightFrom, lightTo, (double) (i - 1) / segments);
            int lightEnd = mix(lightFrom, lightTo, t);

            quad(consumer, pose, previous, point, vertical, red * 0.86F, green * 0.86F, blue * 0.86F, lightStart, lightEnd, side);
            quad(consumer, pose, previous, point, lateral, red, green, blue, lightStart, lightEnd, new Vec3(0.0, 1.0, 0.0));
            previous = point;
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, Vec3 from, Vec3 to, Vec3 offset, float red, float green, float blue, int lightFrom, int lightTo, Vec3 normal) {
        Vec3 a = from.subtract(offset);
        Vec3 b = from.add(offset);
        Vec3 c = to.add(offset);
        Vec3 d = to.subtract(offset);
        float nx = (float) normal.x;
        float ny = (float) normal.y;
        float nz = (float) normal.z;

        vertex(consumer, pose, a, red, green, blue, 0.0F, 1.0F, lightFrom, nx, ny, nz);
        vertex(consumer, pose, b, red, green, blue, 0.0F, 0.0F, lightFrom, nx, ny, nz);
        vertex(consumer, pose, c, red, green, blue, 1.0F, 0.0F, lightTo, nx, ny, nz);
        vertex(consumer, pose, d, red, green, blue, 1.0F, 1.0F, lightTo, nx, ny, nz);

        vertex(consumer, pose, d, red, green, blue, 1.0F, 1.0F, lightTo, -nx, -ny, -nz);
        vertex(consumer, pose, c, red, green, blue, 1.0F, 0.0F, lightTo, -nx, -ny, -nz);
        vertex(consumer, pose, b, red, green, blue, 0.0F, 0.0F, lightFrom, -nx, -ny, -nz);
        vertex(consumer, pose, a, red, green, blue, 0.0F, 1.0F, lightFrom, -nx, -ny, -nz);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 position, float red, float green, float blue, float u, float v, int light, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(red, green, blue, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static int mix(int lightFrom, int lightTo, double t) {
        int block = (int) (LightTexture.block(lightFrom) + (LightTexture.block(lightTo) - LightTexture.block(lightFrom)) * t);
        int sky = (int) (LightTexture.sky(lightFrom) + (LightTexture.sky(lightTo) - LightTexture.sky(lightFrom)) * t);
        return LightTexture.pack(block, sky);
    }

    public static AABB bounds(BlockEntity blockEntity) {
        AABB box = new AABB(blockEntity.getBlockPos());
        Level level = blockEntity.getLevel();
        if (level != null && blockEntity instanceof CableNode node) {
            for (CableLink link : node.links().all()) {
                box = box.minmax(new AABB(link.other()));
            }
        }
        return box.inflate(3.0);
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderCables(blockEntity, poseStack, buffer);
    }

    // Long cables render outside their chunk
    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return bounds(blockEntity);
    }

    public static void renderDebugBox(Level level, Vec3 point, Vec3 renderOrigin, PoseStack poseStack, MultiBufferSource buffer, float size, int color) {
        Vec3 center = point.subtract(renderOrigin);
        int light = LevelRenderer.getLightColor(level, BlockPos.containing(point));

        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        double h = size / 2.0;
        double x0 = center.x - h, x1 = center.x + h;
        double y0 = center.y - h, y1 = center.y + h;
        double z0 = center.z - h, z1 = center.z + h;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        debugFace(consumer, pose, new Vec3(x0, y0, z0), new Vec3(x0, y0, z1), new Vec3(x0, y1, z1), new Vec3(x0, y1, z0), red, green, blue, light, new Vec3(-1, 0, 0));
        debugFace(consumer, pose, new Vec3(x1, y0, z0), new Vec3(x1, y1, z0), new Vec3(x1, y1, z1), new Vec3(x1, y0, z1), red, green, blue, light, new Vec3(1, 0, 0));
        debugFace(consumer, pose, new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), new Vec3(x1, y0, z1), new Vec3(x0, y0, z1), red, green, blue, light, new Vec3(0, -1, 0));
        debugFace(consumer, pose, new Vec3(x0, y1, z0), new Vec3(x0, y1, z1), new Vec3(x1, y1, z1), new Vec3(x1, y1, z0), red, green, blue, light, new Vec3(0, 1, 0));
        debugFace(consumer, pose, new Vec3(x0, y0, z0), new Vec3(x0, y1, z0), new Vec3(x1, y1, z0), new Vec3(x1, y0, z0), red, green, blue, light, new Vec3(0, 0, -1));
        debugFace(consumer, pose, new Vec3(x0, y0, z1), new Vec3(x1, y0, z1), new Vec3(x1, y1, z1), new Vec3(x0, y1, z1), red, green, blue, light, new Vec3(0, 0, 1));
    }

    private static void debugFace(VertexConsumer consumer, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float red, float green, float blue, int light, Vec3 normal) {
        float nx = (float) normal.x;
        float ny = (float) normal.y;
        float nz = (float) normal.z;

        vertex(consumer, pose, a, red, green, blue, 0.0F, 0.0F, light, nx, ny, nz);
        vertex(consumer, pose, b, red, green, blue, 0.0F, 1.0F, light, nx, ny, nz);
        vertex(consumer, pose, c, red, green, blue, 1.0F, 1.0F, light, nx, ny, nz);
        vertex(consumer, pose, d, red, green, blue, 1.0F, 0.0F, light, nx, ny, nz);

        vertex(consumer, pose, d, red, green, blue, 1.0F, 0.0F, light, -nx, -ny, -nz);
        vertex(consumer, pose, c, red, green, blue, 1.0F, 1.0F, light, -nx, -ny, -nz);
        vertex(consumer, pose, b, red, green, blue, 0.0F, 1.0F, light, -nx, -ny, -nz);
        vertex(consumer, pose, a, red, green, blue, 0.0F, 0.0F, light, -nx, -ny, -nz);
    }
}