package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.CableLinks;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.content.block.InsulatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@NothingNullByDefault
public class InsulatorBlockEntity extends SmartBlockEntity implements CableNode {
    private static final double ANCHOR_OFFSET = 0.1;

    private final CableLinks links = new CableLinks();

    public InsulatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public CableLinks links() {
        return this.links;
    }

    @Override
    public Vec3 anchor() {
        return Vec3.atCenterOf(this.worldPosition).relative(getBlockState().getValue(InsulatorBlock.FACING), ANCHOR_OFFSET);
    }

    @Override
    public void cablesChanged() {
        sendData();
        setChanged();
    }

    @Override
    public boolean junction() {
        return true;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.links.save(compound);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.links.load(compound);
    }
}
