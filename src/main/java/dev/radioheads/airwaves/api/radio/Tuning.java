package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import dev.radioheads.airwaves.registry.AirwaveRadioBands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;

@NothingNullByDefault
public record Tuning(long hertz, Holder<RadioBand> band) {
    public static final Tuning DEFAULT = new Tuning(98_100_000L, AirwaveRadioBands.FM_BROADCAST);

    public static final Codec<Tuning> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    Codec.LONG.fieldOf("hertz").forGetter(Tuning::hertz),
                    // Removed bands fall back to FM
                    RegistryFixedCodec.create(AirwaveRegistries.RADIO_BAND)
                            .lenientOptionalFieldOf("band", AirwaveRadioBands.FM_BROADCAST)
                            .forGetter(Tuning::band)
            ).apply(inst, Tuning::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Tuning> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, Tuning::hertz,
                    ByteBufCodecs.holderRegistry(AirwaveRegistries.RADIO_BAND), Tuning::band,
                    Tuning::new
            );

    public Tuning withHertz(long hertz) {
        return new Tuning(hertz, this.band);
    }

    public Tuning clamped() {
        return new Tuning(this.band.value().clamp(this.hertz), this.band);
    }

    public boolean matches(Tuning other) {
        return band.value().equals(other.band.value())
                && band.value().clamp(hertz) == other.band.value().clamp(other.hertz);
    }

    public void writeTo(CompoundTag compound, HolderLookup.Provider registries) {
        CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this)
                .resultOrPartial(err -> Airwaves.LOGGER.error("Failed to encode tuning: {}", err))
                .ifPresent(tag -> compound.put("Tuning", tag));
    }

    public static Tuning readFrom(CompoundTag compound, HolderLookup.Provider registries) {
        if (!compound.contains("Tuning")) {
            return DEFAULT;
        }
        return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), compound.get("Tuning"))
                .resultOrPartial(err -> Airwaves.LOGGER.error("Failed to decode tuning: {}", err))
                .orElse(DEFAULT)
                .clamped();
    }

    public String display() {
        return this.band.value().display(this.hertz);
    }
}
