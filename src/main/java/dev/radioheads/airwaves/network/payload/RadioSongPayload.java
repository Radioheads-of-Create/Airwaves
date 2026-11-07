package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.client.RadioSoundPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

// A jukebox song on a radio channel that stops when song is empty
// Volume is the level at the speaker and ticks is how long the jukebox has played it
// Sent again for the same song when the level changes
public record RadioSongPayload(
        UUID channelId,
        Optional<Holder<SoundEvent>> song,
        Vec3 speaker,
        float volume,
        long ticks
) implements CustomPacketPayload {
    public static final Type<RadioSongPayload> TYPE = new Type<>(Airwaves.asResource("radio_song"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioSongPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RadioSongPayload::channelId,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), RadioSongPayload::song,
            RadioSoundPayload.POSITION_CODEC, RadioSongPayload::speaker,
            ByteBufCodecs.FLOAT, RadioSongPayload::volume,
            ByteBufCodecs.VAR_LONG, RadioSongPayload::ticks,
            RadioSongPayload::new
    );

    public static RadioSongPayload stop(UUID channelId) {
        return new RadioSongPayload(channelId, Optional.empty(), Vec3.ZERO, 0.0F, 0L);
    }

    public void sendTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public static void onReceive(RadioSongPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> RadioSoundPlayer.song(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
