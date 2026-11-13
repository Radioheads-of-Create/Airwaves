package dev.radioheads.airwaves.content.item;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.content.telephone.EarpieceBinding;
import dev.radioheads.airwaves.content.telephone.PhoneNumbers;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

@NothingNullByDefault
public class TelephoneEarpieceItem extends Item {
    public TelephoneEarpieceItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EarpieceBinding binding = stack.get(AirwaveDataComponents.EARPIECE);
        if (binding != null) {
            tooltip.add((binding.number().isEmpty()
                    ? Component.translatable("airwaves.telephone_earpiece.unnumbered")
                    : Component.translatable("airwaves.telephone_earpiece.bound", PhoneNumbers.format(binding.number()))).withStyle(ChatFormatting.GRAY));
        }
    }

    // Dropped earpiece
    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return stack.has(AirwaveDataComponents.EARPIECE) ? Integer.MAX_VALUE : super.getEntityLifespan(stack, level);
    }
}
