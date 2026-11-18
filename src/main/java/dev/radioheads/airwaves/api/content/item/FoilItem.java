package dev.radioheads.airwaves.api.content.item;

import dev.radioheads.airwaves.AirwaveLang;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

@NothingNullByDefault
public class FoilItem extends Item {
    private final Supplier<FoilType> type;

    public FoilItem(Properties properties, Supplier<FoilType> type) {
        super(properties);
        this.type = type;
    }

    public FoilType getFoilType() {
        return type.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int amount = stack.getOrDefault(AirwaveDataComponents.FOIL_AMOUNT, -1);
        if (amount != -1) {
            tooltipComponents.add(AirwaveLang.translate("tooltip.amount.remaining", amount).component().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level level) {
        if (!stack.has(AirwaveDataComponents.FOIL_AMOUNT)) {
            stack.set(AirwaveDataComponents.FOIL_AMOUNT, 250);
        }
    }
}
