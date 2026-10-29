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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public record Tuning(double frequency, Holder<RadioBand> band) {
    public static final Tuning DEFAULT = new Tuning(98.1, AirwaveRadioBands.FM);

    public static final Codec<Tuning> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    Codec.DOUBLE.fieldOf("frequency").forGetter(Tuning::frequency),
                    RegistryFixedCodec.create(AirwaveRegistries.RADIO_BAND)
                            .optionalFieldOf("band", AirwaveRadioBands.FM)
                            .forGetter(Tuning::band)
            ).apply(inst, Tuning::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Tuning> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, Tuning::frequency,
                    ByteBufCodecs.holderRegistry(AirwaveRegistries.RADIO_BAND), Tuning::band,
                    Tuning::new
            );

    public Tuning clamped() {
        return new Tuning(band.value().clamp(frequency), band);
    }

    public boolean matches(Tuning other) {
        return band.value().equals(other.band.value())
                && Double.compare(
                band.value().clamp(frequency),
                other.band.value().clamp(other.frequency)
        ) == 0;
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
                .orElse(DEFAULT);
    }

    public String display() {
        ResourceLocation id = band.unwrapKey()
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalStateException("Radio band is not registered"));
        return band.value().display(frequency, id);
    }

    public Tuning copy() {
        return new Tuning(frequency, band);
    }
}