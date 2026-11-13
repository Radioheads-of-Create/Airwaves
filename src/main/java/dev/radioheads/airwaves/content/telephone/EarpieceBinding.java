package dev.radioheads.airwaves.content.telephone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

// Earpiece pairing
public record EarpieceBinding(UUID earpiece, UUID phone, String number) {
    public static final Codec<EarpieceBinding> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.fieldOf("earpiece").forGetter(EarpieceBinding::earpiece),
            UUIDUtil.CODEC.fieldOf("phone").forGetter(EarpieceBinding::phone),
            Codec.STRING.optionalFieldOf("number", "").forGetter(EarpieceBinding::number)
    ).apply(inst, EarpieceBinding::new));

    public static final StreamCodec<ByteBuf, EarpieceBinding> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, EarpieceBinding::earpiece,
            UUIDUtil.STREAM_CODEC, EarpieceBinding::phone,
            ByteBufCodecs.STRING_UTF8, EarpieceBinding::number,
            EarpieceBinding::new
    );
}
