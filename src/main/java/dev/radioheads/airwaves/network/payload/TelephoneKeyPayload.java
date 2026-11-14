package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.entity.block.TelephoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Dial key press
public record TelephoneKeyPayload(BlockPos pos, int key) implements CustomPacketPayload {
    public static final int ERASE = -1;

    public static final Type<TelephoneKeyPayload> TYPE = new Type<>(Airwaves.asResource("telephone_key"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TelephoneKeyPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TelephoneKeyPayload::pos,
            ByteBufCodecs.VAR_INT, TelephoneKeyPayload::key,
            TelephoneKeyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onKey(TelephoneKeyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.level().isLoaded(payload.pos)
                    || !(player.level().getBlockEntity(payload.pos) instanceof TelephoneBlockEntity phone)
                    || !player.getEyePosition().closerThan(Vec3.atCenterOf(payload.pos), TelephoneBlockEntity.REACH)) {
                return;
            }
            if (payload.key == ERASE) {
                phone.erase();
            } else if (payload.key >= 0 && payload.key <= 8) {
                phone.press(payload.key, player);
            }
        });
    }
}
