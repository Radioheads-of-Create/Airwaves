package dev.radioheads.airwaves.content.item;

import dev.radioheads.airwaves.AirwaveLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class OldModelNotCompleteYetBlockItem extends BlockItem {
    public OldModelNotCompleteYetBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(AirwaveLang.translate("tooltip.old_content_no_functionallity").component().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
    }
}
