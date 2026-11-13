package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.item.CableCutterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Cable cut request
public record CableCutPayload(BlockPos from, BlockPos to) implements CustomPacketPayload {
    public static final Type<CableCutPayload> TYPE = new Type<>(Airwaves.asResource("cable_cut"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CableCutPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CableCutPayload::from,
            BlockPos.STREAM_CODEC, CableCutPayload::to,
            CableCutPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onCut(CableCutPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                CableCutterItem.cut(player, payload.from, payload.to);
            }
        });
    }
}
