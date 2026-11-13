package dev.radioheads.airwaves.registry;

import com.mojang.serialization.Codec;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.data.CapacitorData;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.content.telephone.EarpieceBinding;
import dev.radioheads.airwaves.content.telephone.TelephoneData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AirwaveDataComponents {
    private AirwaveDataComponents() {}

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Airwaves.MOD_ID);

    public static final DataComponentType<Tuning> TUNING = register("tuning", Tuning.CODEC, Tuning.STREAM_CODEC);
    public static final DataComponentType<CapacitorData> CAPACITOR_DATA = register("capacitor", CapacitorData.CODEC, CapacitorData.STREAM_CODEC);

    public static final DataComponentType<BlockPos> CABLE_START = register("cable_start", BlockPos.CODEC, BlockPos.STREAM_CODEC);

    public static final DataComponentType<TelephoneData> TELEPHONE = register("telephone", TelephoneData.CODEC, TelephoneData.STREAM_CODEC);
    public static final DataComponentType<EarpieceBinding> EARPIECE = register("earpiece", EarpieceBinding.CODEC, EarpieceBinding.STREAM_CODEC);

    public static final DataComponentType<Integer> FOIL_AMOUNT = registerNonNegativeInt("foil_amount");
    public static final DataComponentType<Integer> SPOOL_AMOUNT = registerNonNegativeInt("spool_amount");

    private static DataComponentType<Integer> registerNonNegativeInt(String name) {
        return register(name, ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.VAR_INT);
    }

    private static <T> DataComponentType<T> register(String name, Codec<T> persistent, StreamCodec<? super RegistryFriendlyByteBuf, T> network) {
        DataComponentType<T> type = new DataComponentType.Builder<T>().persistent(persistent).networkSynchronized(network).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }
}
