package dev.radioheads.airwaves.content.item;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.content.screen.ManualScreen;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@NothingNullByDefault
@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class FieldManualItem extends Item {
    private static final String RECEIVED = "airwaves_manual";

    public FieldManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ManualScreen.open();
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.7F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag data = event.getEntity().getPersistentData();
        if (!data.getBoolean(RECEIVED)) {
            data.putBoolean(RECEIVED, true);
            event.getEntity().getInventory().add(AirwaveItems.FIELD_MANUAL.asStack());
        }
    }
}
