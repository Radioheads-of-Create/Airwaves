package dev.radioheads.airwaves.content.item;

import dev.radioheads.airwaves.api.cable.CableAim;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.content.item.CableItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

// Cuts the aimed cable
public class CableCutterItem extends Item {
    // Extra reach for latency
    private static final double SLACK = 1.5;

    public CableCutterItem(Properties properties) {
        super(properties);
    }

    public static boolean holding(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).getItem() instanceof CableCutterItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean cut(ServerPlayer player, BlockPos from, BlockPos to) {
        Level level = player.level();
        CableNode a = Cables.node(level, from);
        CableNode b = Cables.node(level, to);
        if (!holding(player) || (a == null && b == null)) {
            return false;
        }
        Vec3 fromAnchor = a != null ? a.anchor() : Vec3.atCenterOf(from);
        Vec3 toAnchor = b != null ? b.anchor() : Vec3.atCenterOf(to);
        if (CableAim.distance(player.getEyePosition(), fromAnchor, toAnchor) > player.blockInteractionRange() + SLACK) {
            return false;
        }

        Optional<ItemStack> spools = Cables.cut(level, from, to);
        if (spools.isEmpty()) {
            return false;
        }
        player.getInventory().placeItemBackInInventory(spools.get());
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(CableItem.hint("cut"), true);
        return true;
    }
}
