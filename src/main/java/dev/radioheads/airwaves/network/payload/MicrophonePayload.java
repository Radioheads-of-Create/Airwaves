package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.entity.block.MicrophoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MicrophonePayload(BlockPos pos, Action action) implements CustomPacketPayload {
    public static final Type<MicrophonePayload> TYPE = new Type<>(Airwaves.asResource("microphone"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MicrophonePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MicrophonePayload::pos,
            ByteBufCodecs.VAR_INT.map(i -> Action.values()[Math.floorMod(i, Action.values().length)], Action::ordinal), MicrophonePayload::action,
            MicrophonePayload::new
    );

    public enum Action {
        PRESS,
        RELEASE,
        TURN_UP,
        TURN_DOWN
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onUse(MicrophonePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.level().isLoaded(payload.pos)
                    || !(player.level().getBlockEntity(payload.pos) instanceof MicrophoneBlockEntity microphone)) {
                return;
            }
            // Release skips the reach check
            if (payload.action != Action.RELEASE && player.position().distanceTo(Vec3.atCenterOf(payload.pos)) > MicrophoneBlockEntity.REACH) {
                return;
            }
            switch (payload.action) {
                case PRESS -> microphone.press(player);
                case RELEASE -> microphone.release(player);
                case TURN_UP -> microphone.turn(player, 1);
                case TURN_DOWN -> microphone.turn(player, -1);
            }
        });
    }
}
