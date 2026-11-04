package dev.radioheads.airwaves.api.content.item;

import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class CableItem extends Item {
    public static final int HINT_COLOR = 0xFFC53D;

    private final Supplier<CableType> type;

    public CableItem(Properties properties, Supplier<CableType> type) {
        super(properties);
        this.type = type;
    }

    public static Component hint(String key, Object... args) {
        return Component.translatable("airwaves.cable." + key, args).withColor(HINT_COLOR);
    }

    private static Component refused(String key, Object... args) {
        return Component.translatable("airwaves.cable." + key, args).withStyle(ChatFormatting.RED);
    }

    public boolean hasSpools(Player player, int count) {
        return player.hasInfiniteMaterials() || player.getInventory().countItem(this) >= count;
    }

    public Optional<Component> refusal(Player player, Level level, BlockPos start, BlockPos end) {
        Optional<Component> refusal = Cables.refusal(Cables.node(level, start), start, Cables.node(level, end), end)
                .map(reason -> refused(reason.name().toLowerCase(Locale.ROOT), Cables.MAX_LENGTH));
        if (refusal.isEmpty() && !hasSpools(player, Cables.spools(Cables.length(start, end)))) {
            return Optional.of(refused("not_enough"));
        }
        return refusal;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos here = context.getClickedPos();
        CableNode node = Cables.node(level, here);
        if (player == null || node == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos start = stack.get(AirwaveDataComponents.CABLE_START);
        if (start != null && Cables.node(level, start) == null) {
            stack.remove(AirwaveDataComponents.CABLE_START);
            start = null;
        }

        if (start == null && player.isShiftKeyDown()) {
            Cables.unlink(level, here).forEach(cable -> player.getInventory().placeItemBackInInventory(cable));
            level.playSound(null, here, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
        } else if (start == null) {
            stack.set(AirwaveDataComponents.CABLE_START, here);
            player.displayClientMessage(hint("select"), true);
        } else if (start.equals(here) || player.isShiftKeyDown()) {
            clearSelection(player, stack);
        } else {
            connect(player, stack, level, start, here);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !stack.has(AirwaveDataComponents.CABLE_START)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            clearSelection(player, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void clearSelection(Player player, ItemStack stack) {
        stack.remove(AirwaveDataComponents.CABLE_START);
        player.displayClientMessage(hint("cleared"), true);
    }

    private void connect(Player player, ItemStack stack, Level level, BlockPos start, BlockPos end) {
        Optional<Component> refusal = refusal(player, level, start, end);
        if (refusal.isPresent()) {
            player.displayClientMessage(refusal.get(), true);
            return;
        }

        stack.remove(AirwaveDataComponents.CABLE_START);
        Cables.link(level, start, end, this.type.get());
        takeSpools(player, stack, Cables.spools(Cables.length(start, end)));
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(hint("connected"), true);
    }

    private void takeSpools(Player player, ItemStack held, int count) {
        if (player.hasInfiniteMaterials()) {
            return;
        }

        int fromHand = Math.min(count, held.getCount());
        held.shrink(fromHand);
        player.getInventory().clearOrCountMatchingItems(stack -> stack.is(this), count - fromHand, player.inventoryMenu.getCraftSlots());
    }
}
