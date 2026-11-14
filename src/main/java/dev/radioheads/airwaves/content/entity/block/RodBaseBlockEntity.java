package dev.radioheads.airwaves.content.entity.block;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import dev.radioheads.airwaves.api.cable.LineType;
import dev.radioheads.airwaves.content.block.AntennaRodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@NothingNullByDefault
public class RodBaseBlockEntity extends InsulatorBlockEntity {
    public RodBaseBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public Vec3 anchor() {
        return Vec3.atBottomCenterOf(this.worldPosition).add(0.0, 0.65, 0.0);
    }

    @Override
    public LineType line() {
        return LineType.RADIO;
    }

    // No goggle lines
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    @Override
    public boolean settled() {
        return AntennaRodBlock.fullyLoaded(this.level, this.worldPosition);
    }

    @Override
    public Optional<AntennaProfile> feeds() {
        Set<BlockPos> rods = AntennaRodBlock.connectedTo(this.level, this.worldPosition);
        if (rods.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AntennaProfile.of(rods, this.worldPosition.above(), pos -> ((AntennaRodBlock) this.level.getBlockState(pos).getBlock()).resistivity()));
    }
}
