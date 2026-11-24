package dev.radioheads.airwaves.content.item;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.item.Tunable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

@NothingNullByDefault
public class ChainCopperBulbItem extends BlockItem implements Tunable {
    public ChainCopperBulbItem(Block block, Properties properties) {
        super(block, properties);
    }
}
