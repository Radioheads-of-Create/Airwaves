package dev.radioheads.airwaves.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.client.cable.CableRenderer;
import dev.radioheads.airwaves.client.render.foil_application.FoilApplicationRenderer;
import dev.radioheads.airwaves.client.render.foil_application.FoilApplicationVisual;
import dev.radioheads.airwaves.client.render.hf.HfTransceiverRenderer;
import dev.radioheads.airwaves.client.render.microphone.MicrophoneRenderer;
import dev.radioheads.airwaves.client.render.telephone.TelephoneRenderer;
import dev.radioheads.airwaves.client.render.transmitter.TransmitterVisual;
import dev.radioheads.airwaves.content.entity.block.*;

public final class AirwaveBlockEntities {
    private AirwaveBlockEntities() {}

    public static final BlockEntityEntry<RadioBlockEntity> RADIO =
            Airwaves.REGISTRATE.blockEntity("radio", RadioBlockEntity::new)
                    .validBlock(AirwaveBlocks.RADIO::get)
                    .register();

    public static final BlockEntityEntry<FoilApplicatorBlockEntity> FOIL_APPLICATOR =
            Airwaves.REGISTRATE.blockEntity("foil_applicator", FoilApplicatorBlockEntity::new)
                    .visual(() -> FoilApplicationVisual::new)
                    .validBlock(AirwaveBlocks.FOIL_APPLICATOR::get)
                    .renderer(() -> FoilApplicationRenderer::new)
                    .register();

    public static final BlockEntityEntry<InsulatorBlockEntity> INSULATOR = Airwaves.REGISTRATE.blockEntity("insulator", InsulatorBlockEntity::new)
            .validBlocks(AirwaveBlocks.ANDESITE_INSULATOR, AirwaveBlocks.BRASS_INSULATOR)
            .renderer(() -> CableRenderer::new)
            .register();

    public static final BlockEntityEntry<RodBaseBlockEntity> ROD_BASE = Airwaves.REGISTRATE.blockEntity("rod_base", RodBaseBlockEntity::new)
            .validBlocks(AirwaveBlocks.COPPER_ROD_BASE, AirwaveBlocks.GOLD_ROD_BASE, AirwaveBlocks.SILVER_ROD_BASE)
            .renderer(() -> CableRenderer::new)
            .register();

    public static final BlockEntityEntry<HfTransceiverBlockEntity> HF_TRANSCEIVER = Airwaves.REGISTRATE.blockEntity("hf_transceiver", HfTransceiverBlockEntity::new)
            .visual(() -> TransmitterVisual::new)
            .validBlock(AirwaveBlocks.HF_TRANSCEIVER::get)
            .renderer(() -> HfTransceiverRenderer::new)
            .register();

    public static final BlockEntityEntry<MicrophoneBlockEntity> MICROPHONE = Airwaves.REGISTRATE.blockEntity("microphone", MicrophoneBlockEntity::new)
            .validBlock(AirwaveBlocks.MICROPHONE::get)
            .renderer(() -> MicrophoneRenderer::new)
            .register();

    public static final BlockEntityEntry<TelephoneBlockEntity> TELEPHONE = Airwaves.REGISTRATE.blockEntity("telephone", TelephoneBlockEntity::new)
            .validBlock(AirwaveBlocks.TELEPHONE::get)
            .renderer(() -> TelephoneRenderer::new)
            .register();

    public static void init() {}
}
