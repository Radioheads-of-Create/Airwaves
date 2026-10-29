package dev.radioheads.airwaves.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.entity.block.RadioBlockEntity;

public final class AirwaveBlockEntities {
    private AirwaveBlockEntities() {}

    public static final BlockEntityEntry<RadioBlockEntity> RADIO = Airwaves.REGISTRATE.blockEntity("radio", RadioBlockEntity::new)
            .validBlock(AirwaveBlocks.RADIO::get)
            .register();

    public static void init() {}
}
