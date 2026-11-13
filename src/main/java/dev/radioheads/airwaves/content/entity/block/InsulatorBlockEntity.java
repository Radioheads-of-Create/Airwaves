package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.CableLinks;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.cable.LineSide;
import dev.radioheads.airwaves.api.cable.LineType;
import dev.radioheads.airwaves.content.block.InsulatorBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

@NothingNullByDefault
public class InsulatorBlockEntity extends SmartBlockEntity implements CableNode, IHaveGoggleInformation {
    private static final double ANCHOR_OFFSET = 0.1;

    private final CableLinks links = new CableLinks();
    // Line type
    private LineType wired = LineType.NONE;
    private LineSide side = LineSide.NONE;

    public InsulatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        if (this.level instanceof ServerLevel server) {
            Cables.loaded(server, this.worldPosition, this);
        }
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
    public LineType line() {
        return LineType.NONE;
    }

    @Override
    public LineType wiredLine() {
        return line() != LineType.NONE ? line() : this.wired;
    }

    @Override
    public void lineChanged(LineType line, LineSide side) {
        if (line != this.wired || side != this.side) {
            this.wired = line;
            this.side = side;
            sendData();
        }
    }

    // Goggle lines
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LineType line = wiredLine();
        if (line == LineType.NONE) {
            return false;
        }
        tooltip.add(Component.literal("    ").append(getBlockState().getBlock().getName()));
        tooltip.add(Component.literal("     ").append(Component.translatable("airwaves.insulator.in").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("     ").append(Component.translatable("airwaves.line." + line.name().toLowerCase(Locale.ROOT) + "_network")
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)));
        if (line == LineType.TELEPHONE && this.side != LineSide.NONE) {
            tooltip.add(Component.literal("     ").append(Component.translatable("airwaves.line." + this.side.name().toLowerCase(Locale.ROOT))
                    .withStyle(this.side == LineSide.TX ? ChatFormatting.BLUE : ChatFormatting.YELLOW)));
        }
        return true;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.links.save(compound);
        if (clientPacket) {
            compound.putByte("Line", (byte) this.wired.ordinal());
            compound.putByte("Side", (byte) this.side.ordinal());
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.links.load(compound);
        if (clientPacket) {
            this.wired = LineType.of(compound.getByte("Line"));
            this.side = LineSide.of(compound.getByte("Side"));
        }
    }
}
