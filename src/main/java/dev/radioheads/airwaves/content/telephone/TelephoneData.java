package dev.radioheads.airwaves.content.telephone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

// Telephone item data
public record TelephoneData(UUID phone, String number, Optional<UUID> earpiece) {
    public static final Codec<TelephoneData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.fieldOf("phone").forGetter(TelephoneData::phone),
            Codec.STRING.optionalFieldOf("number", "").forGetter(TelephoneData::number),
            UUIDUtil.CODEC.optionalFieldOf("earpiece").forGetter(TelephoneData::earpiece)
    ).apply(inst, TelephoneData::new));

    public static final StreamCodec<ByteBuf, TelephoneData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TelephoneData::phone,
            ByteBufCodecs.STRING_UTF8, TelephoneData::number,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), TelephoneData::earpiece,
            TelephoneData::new
    );
}
