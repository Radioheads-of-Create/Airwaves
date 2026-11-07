package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.CableLinks;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.content.block.HfTransceiverPanel;
import dev.radioheads.airwaves.content.block.MicrophoneBlock;
import dev.radioheads.airwaves.network.Microphones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NothingNullByDefault
public class MicrophoneBlockEntity extends SmartBlockEntity implements CableNode {
    private static final int CABLE_SCAN_TICKS = 20;
    // Tap or hold to talk cutoff
    public static final int HOLD_TICKS = 8;
    public static final double REACH = 6.0;
    public static final float KNOB_STEP = 0.05F;

    private final CableLinks links = new CableLinks();
    private List<BlockPos> devices = List.of();
    private boolean on;
    private float gain = 1.0F;
    @Nullable
    private UUID holder;
    private boolean wasOn;
    private long pressedAt;

    public MicrophoneBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        setLazyTickRate(CABLE_SCAN_TICKS);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!this.level.isClientSide) {
            Microphones.add(this.level, this);
            scanCables();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (this.level != null && !this.level.isClientSide) {
            Microphones.remove(this.level, this);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (!this.level.isClientSide) {
            scanCables();
        }
    }

    private void scanCables() {
        this.devices = Cables.devices(pos -> Cables.node(this.level, pos), this.worldPosition);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            holdArm();
            return;
        }

        if (this.holder != null) {
            Player player = this.level.getPlayerByUUID(this.holder);
            if (player == null || !player.isAlive() || player.position().distanceTo(Vec3.atCenterOf(this.worldPosition)) > REACH) {
                this.holder = null;
                switchTo(false);
            }
        }

        BlockState state = getBlockState().setValue(MicrophoneBlock.ON_AIR, this.on);
        if (state != getBlockState()) {
            this.level.setBlockAndUpdate(this.worldPosition, state);
        }
    }

    // Holder's arm frozen part way through a swing
    private void holdArm() {
        Player player = this.holder == null ? null : this.level.getPlayerByUUID(this.holder);
        if (player != null) {
            player.swinging = true;
            player.swingingArm = InteractionHand.MAIN_HAND;
            player.swingTime = player.getCurrentSwingDuration() / 3 - 1;
        }
    }

    public void press(ServerPlayer player) {
        if (this.holder != null && !this.holder.equals(player.getUUID())) {
            return;
        }
        this.wasOn = this.on;
        this.holder = player.getUUID();
        this.pressedAt = this.level.getGameTime();
        switchTo(true);
    }

    public void release(ServerPlayer player) {
        if (!player.getUUID().equals(this.holder)) {
            return;
        }
        boolean tapped = this.level.getGameTime() - this.pressedAt < HOLD_TICKS;
        this.holder = null;
        switchTo(tapped && !this.wasOn);
    }

    private void switchTo(boolean on) {
        if (on != this.on) {
            this.level.playSound(null, this.worldPosition, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, on ? 1.4F : 1.1F);
        }
        this.on = on;
        sendData();
        setChanged();
    }

    public void turn(ServerPlayer player, int delta) {
        this.gain = Mth.clamp(Math.round((this.gain + delta * KNOB_STEP) / KNOB_STEP) * KNOB_STEP, 0.0F, 1.0F);
        player.displayClientMessage(Component.translatable("airwaves.microphone.output", Math.round(this.gain * 100.0F)), true);
        this.level.playSound(null, this.worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.1F, 0.6F + this.gain);
        sendData();
        setChanged();
    }

    public boolean on() {
        return this.on;
    }

    public float gain() {
        return this.gain;
    }

    public UUID id() {
        return RadioStation.blockId(this.level, this.worldPosition);
    }

    private Direction facing() {
        return getBlockState().getValue(MicrophoneBlock.FACING);
    }

    private Vec3 model(Vec3 pixels) {
        return Vec3.atLowerCornerOf(this.worldPosition).add(HfTransceiverPanel.toBlock(facing(), pixels.x, pixels.y, pixels.z));
    }

    public Vec3 capsule() {
        return model(MicrophoneBlock.CAPSULE);
    }

    // Nearest cabled transmitter
    public Optional<RadioStation> station() {
        for (BlockPos pos : this.devices) {
            if (this.level.isLoaded(pos) && this.level.getBlockEntity(pos) instanceof RadioStation station && station.transmits()) {
                return Optional.of(station);
            }
        }
        return Optional.empty();
    }

    @Override
    public CableLinks links() {
        return this.links;
    }

    @Override
    public Vec3 anchor() {
        return model(MicrophoneBlock.SOCKET);
    }

    @Override
    public void cablesChanged() {
        if (!this.level.isClientSide) {
            scanCables();
        }
        sendData();
        setChanged();
    }

    @Override
    public int maxLinks() {
        return 1;
    }

    @Override
    public boolean junction() {
        return true;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.links.save(compound);
        // State before the press
        compound.putBoolean("On", clientPacket || this.holder == null ? this.on : this.wasOn);
        compound.putFloat("Gain", this.gain);
        if (clientPacket && this.holder != null) {
            compound.putUUID("Holder", this.holder);
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.links.load(compound);
        this.on = compound.getBoolean("On");
        this.gain = compound.contains("Gain") ? compound.getFloat("Gain") : 1.0F;
        if (clientPacket) {
            this.holder = compound.hasUUID("Holder") ? compound.getUUID("Holder") : null;
        }
    }
}
