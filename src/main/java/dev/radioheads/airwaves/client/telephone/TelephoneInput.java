package dev.radioheads.airwaves.client.telephone;

import com.mojang.blaze3d.platform.InputConstants;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.TelephoneBlock;
import dev.radioheads.airwaves.network.payload.TelephoneKeyPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Number key dialing
@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public final class TelephoneInput {
    private static final int NONE = Integer.MIN_VALUE;
    private static final List<InputConstants.Key> SWALLOWED = new ArrayList<>();

    private TelephoneInput() {}

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        int key = key(event.getKey());
        BlockPos target = key == NONE ? null : target(Minecraft.getInstance());
        if (target == null) {
            return;
        }
        SWALLOWED.add(InputConstants.getKey(event.getKey(), event.getScanCode()));
        PacketDistributor.sendToServer(new TelephoneKeyPayload(target, key));
    }

    // Swallowed hotbar clicks
    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        if (SWALLOWED.isEmpty()) {
            return;
        }
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            for (InputConstants.Key key : SWALLOWED) {
                if (mapping.isActiveAndMatches(key)) {
                    while (mapping.consumeClick()) {
                    }
                    mapping.setDown(false);
                }
            }
        }
        SWALLOWED.clear();
    }

    private static int key(int glfw) {
        if (glfw >= GLFW.GLFW_KEY_1 && glfw <= GLFW.GLFW_KEY_8) {
            return glfw - GLFW.GLFW_KEY_1 + 1;
        }
        if (glfw >= GLFW.GLFW_KEY_KP_1 && glfw <= GLFW.GLFW_KEY_KP_8) {
            return glfw - GLFW.GLFW_KEY_KP_1 + 1;
        }
        if (glfw == GLFW.GLFW_KEY_ENTER || glfw == GLFW.GLFW_KEY_KP_ENTER) {
            return TelephoneBlock.CALL;
        }
        if (glfw == GLFW.GLFW_KEY_BACKSPACE) {
            return TelephoneKeyPayload.ERASE;
        }
        return NONE;
    }

    @Nullable
    private static BlockPos target(Minecraft minecraft) {
        if (minecraft.screen != null || minecraft.player == null || minecraft.level == null || minecraft.player.isSpectator()) {
            return null;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
                || !(minecraft.level.getBlockState(hit.getBlockPos()).getBlock() instanceof TelephoneBlock)) {
            return null;
        }
        return hit.getBlockPos();
    }
}
