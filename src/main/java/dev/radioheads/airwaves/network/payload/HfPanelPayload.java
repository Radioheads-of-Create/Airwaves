package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.HfTransceiverControl;
import dev.radioheads.airwaves.content.entity.block.HfTransceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HfPanelPayload(BlockPos pos, int control, int delta) implements CustomPacketPayload {
    public static final Type<HfPanelPayload> TYPE = new Type<>(Airwaves.asResource("hf_panel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HfPanelPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, HfPanelPayload::pos,
            ByteBufCodecs.VAR_INT, HfPanelPayload::control,
            ByteBufCodecs.VAR_INT, HfPanelPayload::delta,
            HfPanelPayload::new
    );
    private static final double REACH = 8.0;
    private static final int MAX_DELTA = 10;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onTurn(HfPanelPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            HfTransceiverControl[] controls = HfTransceiverControl.values();
            if (payload.control < 0 || payload.control >= controls.length || payload.delta == 0) {
                return;
            }
            if (player.position().distanceTo(Vec3.atCenterOf(payload.pos)) > REACH || !player.level().isLoaded(payload.pos)) {
                return;
            }
            if (player.level().getBlockEntity(payload.pos) instanceof HfTransceiverBlockEntity transceiver) {
                transceiver.turn(controls[payload.control], Mth.clamp(payload.delta, -MAX_DELTA, MAX_DELTA));
            }
        });
    }
}
