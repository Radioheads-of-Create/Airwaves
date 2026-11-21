package dev.radioheads.airwaves.api.recipe.foil;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.function.Function;

public class FoilApplicationRecipeParams extends ProcessingRecipeParams {
    public int foilUsage = 1;

    public static final MapCodec<FoilApplicationRecipeParams> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            codec(FoilApplicationRecipeParams::new).forGetter(Function.identity()),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("foil_usage", 1).forGetter(FoilApplicationRecipeParams::getFoilUsage)
    ).apply(inst, (params, foilUsage) -> {
        params.foilUsage = foilUsage;
        return params;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, FoilApplicationRecipeParams> STREAM_CODEC = streamCodec(FoilApplicationRecipeParams::new);

    protected int getFoilUsage() {
        return foilUsage;
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        ByteBufCodecs.INT.encode(buffer, foilUsage);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        foilUsage = ByteBufCodecs.INT.decode(buffer);
    }
}
