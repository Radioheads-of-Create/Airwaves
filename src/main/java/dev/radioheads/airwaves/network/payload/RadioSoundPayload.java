package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.client.radio.RadioSoundPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// A sound picked up by a microphone and played at a speaker
public record RadioSoundPayload(
        Holder<SoundEvent> sound,
        SoundSource source,
        Vec3 speaker,
        float volume,
        float pitch
) implements CustomPacketPayload {
    public static final Type<RadioSoundPayload> TYPE = new Type<>(Airwaves.asResource("radio_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> POSITION_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioSoundPayload> CODEC = StreamCodec.composite(
            SoundEvent.STREAM_CODEC, RadioSoundPayload::sound,
            ByteBufCodecs.VAR_INT.map(i -> SoundSource.values()[i], SoundSource::ordinal), RadioSoundPayload::source,
            POSITION_CODEC, RadioSoundPayload::speaker,
            ByteBufCodecs.FLOAT, RadioSoundPayload::volume,
            ByteBufCodecs.FLOAT, RadioSoundPayload::pitch,
            RadioSoundPayload::new
    );

    public void sendTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public static void onReceive(RadioSoundPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> RadioSoundPlayer.play(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
