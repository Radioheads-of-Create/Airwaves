package dev.radioheads.airwaves.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.client.CableRenderer;
import dev.radioheads.airwaves.client.render.foil_application.FoilApplicationRenderer;
import dev.radioheads.airwaves.client.render.foil_application.FoilApplicationVisual;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.content.entity.block.InsulatorBlockEntity;
import dev.radioheads.airwaves.content.entity.block.RadioBlockEntity;
import dev.radioheads.airwaves.content.entity.block.RodBaseBlockEntity;

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

    public static void init() {}
}
