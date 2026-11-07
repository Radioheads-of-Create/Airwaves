package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.content.menu.TuningTableMenu;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TunePayload(Tuning tuning) implements CustomPacketPayload {
    public static final Type<TunePayload> TYPE = new Type<>(Airwaves.asResource("tune"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TunePayload> CODEC = StreamCodec.composite(
            Tuning.STREAM_CODEC, TunePayload::tuning,
            TunePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onTune(TunePayload payload, IPayloadContext ctx) {
        Player plr = ctx.player();
        if (plr instanceof ServerPlayer player) {
            AbstractContainerMenu container = player.containerMenu;
            if (container instanceof TuningTableMenu menu) {
                ItemStack stack = menu.getSlot(0).getItem();

                if (stack.isEmpty()) {
                    return;
                }

                stack.set(AirwaveDataComponents.TUNING, payload.tuning.clamped());
                menu.broadcastChanges();
            }
        }
    }
}
