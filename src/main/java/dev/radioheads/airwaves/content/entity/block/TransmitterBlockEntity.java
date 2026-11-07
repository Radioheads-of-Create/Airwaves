package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import dev.radioheads.airwaves.api.cable.CableLinks;
import dev.radioheads.airwaves.api.cable.CableNode;
import dev.radioheads.airwaves.api.cable.Cables;
import dev.radioheads.airwaves.api.cable.FeedLine;
import dev.radioheads.airwaves.api.link.LinkEnd;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.network.RadioNetwork;
import dev.radioheads.airwaves.network.RadioVoice;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NothingNullByDefault
public abstract class TransmitterBlockEntity extends KineticBlockEntity implements RadioStation, CableNode {
    public static final double BASE_STRESS = 4.0;
    public static final double NOISE_FIGURE_DB = 8.0;
    private static final double FULL_POWER_STRESS = 32.0;
    private static final int MAX_POWER_STEPS = 100;
    private static final int ANTENNA_SCAN_TICKS = 20;

    private final CableLinks links = new CableLinks();
    private AntennaProfile aerial;
    private FeedLine feed = FeedLine.NONE;
    private Tuning tuning;
    private float quality;
    private ScrollValueBehaviour power;

    public TransmitterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.aerial = AntennaProfile.bare(Vec3.atCenterOf(pos));
        this.tuning = block().defaultTuning();
        setLazyTickRate(ANTENNA_SCAN_TICKS);
    }

    private HfTransceiverBlock block() {
        return (HfTransceiverBlock) getBlockState().getBlock();
    }

    protected int powerSteps() {
        return Math.min(block().ratedWatts(), MAX_POWER_STEPS);
    }

    protected double watts() {
        return this.power.getValue() * (double) block().ratedWatts() / powerSteps() * Math.pow(10.0, this.feed.amplifierDb() / 10.0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.power = new ScrollValueBehaviour(Component.translatable("airwaves.transmitter.power"), this,
                new CenteredSideValueBoxTransform((state, direction) -> direction == state.getValue(HfTransceiverBlock.HORIZONTAL_FACING).getOpposite()));
        this.power.between(1, powerSteps());
        this.power.value = Math.max(1, powerSteps() / 10);
        this.power.withFormatter(step -> step * block().ratedWatts() / powerSteps() + " W");
        this.power.withCallback(step -> updateStress());
        behaviours.add(this.power);
    }

    private void updateStress() {
        if (hasNetwork()) {
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        }
    }

    @Override
    public float calculateStressApplied() {
        double share = (double) this.power.getValue() / powerSteps();
        float impact = (float) (BASE_STRESS + (FULL_POWER_STRESS - BASE_STRESS) * share);
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!this.level.isClientSide) {
            RadioNetwork.add(this.level, this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (this.level != null && !this.level.isClientSide) {
            RadioNetwork.remove(this.level, this);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (!this.level.isClientSide) {
            scanAntenna();
        }
    }

    private boolean scanAntenna() {
        Cables.Survey survey = Cables.survey(pos -> Cables.node(this.level, pos), this.level::isLoaded, this.worldPosition);
        if (!survey.complete()) {
            return false;
        }

        Optional<Cables.Feed> feed = survey.feed();
        AntennaProfile found = feed.map(Cables.Feed::antenna).orElseGet(() -> AntennaProfile.bare(position()));
        FeedLine line = feed.map(Cables.Feed::line).orElse(FeedLine.NONE);
        boolean changed = found.shape() != this.aerial.shape() || found.lengthBlocks() != this.aerial.lengthBlocks()
                || found.elements() != this.aerial.elements() || line.amplifierDb() != this.feed.amplifierDb();
        this.aerial = found;
        this.feed = line;
        if (changed) {
            sendData();
        }
        return true;
    }

    @Override
    public CableLinks links() {
        return this.links;
    }

    @Override
    public Vec3 anchor() {
        return Vec3.atCenterOf(this.worldPosition);
    }

    @Override
    public void cablesChanged() {
        scanAntenna();
        sendData();
        setChanged();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            boolean speaking = transmits() && RadioVoice.speaking(id());
            BlockState state = getBlockState().setValue(HfTransceiverBlock.ACTIVE, transmits()).setValue(HfTransceiverBlock.ON_AIR, speaking);
            if (state != getBlockState()) {
                this.level.setBlockAndUpdate(this.worldPosition, state);
            }
        }
    }

    @Override
    public UUID id() {
        return RadioStation.blockId(this.level, this.worldPosition);
    }

    @Override
    public Vec3 antenna() {
        return this.aerial.radiator();
    }

    @Override
    public Vec3 position() {
        return Vec3.atCenterOf(this.worldPosition);
    }

    @Override
    public long hertz() {
        return this.tuning.hertz();
    }

    @Override
    public Holder<Modulation> modulation() {
        return this.tuning.band().value().modulation();
    }

    @Override
    public LinkEnd linkEnd(double mhz, double heightBlocks, Vec3 toward) {
        return this.aerial.linkEnd(watts(), this.feed.lossDb(mhz), this.feed.noiseFigureDb(mhz, NOISE_FIGURE_DB), heightBlocks, mhz, toward);
    }

    protected boolean powered() {
        return getSpeed() != 0.0F && !isOverStressed();
    }

    protected Tuning tuning() {
        return this.tuning;
    }

    protected void retune(Tuning tuning) {
        this.tuning = tuning.clamped();
    }

    protected ScrollValueBehaviour powerSetting() {
        return this.power;
    }

    protected float quality() {
        return this.quality;
    }

    protected AntennaProfile aerial() {
        return this.aerial;
    }

    protected FeedLine feed() {
        return this.feed;
    }

    @Override
    public boolean transmits() {
        return powered();
    }

    @Override
    public boolean onAir() {
        return transmits() && RadioVoice.speaking(id());
    }

    @Override
    public boolean receives() {
        return powered();
    }

    @Override
    public void hear(float quality) {
        if (this.quality != quality) {
            this.quality = quality;
            sendData();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("    " + this.tuning.display()));
        tooltip.add(Component.literal("    " + Math.round(watts()) + " W"));
        tooltip.add(Component.literal("    ").append(antennaLine()));
        if (receives()) {
            tooltip.add(Component.literal("    ").append(this.quality > 0.0F
                    ? Component.translatable("airwaves.radio.signal", Math.round(this.quality * 100.0F))
                    : Component.translatable("airwaves.radio.no_signal")));
        }
        return true;
    }

    protected Component antennaLine() {
        return switch (this.aerial.shape()) {
            case NONE -> Component.translatable("airwaves.antenna.none");
            case WHIP -> Component.translatable("airwaves.antenna.whip", (int) this.aerial.lengthBlocks());
            case DIPOLE -> Component.translatable("airwaves.antenna.dipole", (int) this.aerial.lengthBlocks());
            case YAGI -> Component.translatable("airwaves.antenna.yagi", this.aerial.elements());
        };
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.tuning.writeTo(compound, registries);
        this.links.save(compound);
        if (clientPacket) {
            compound.putFloat("Quality", this.quality);
            compound.putInt("AntennaShape", this.aerial.shape().ordinal());
            compound.putDouble("AntennaLength", this.aerial.lengthBlocks());
            compound.putInt("AntennaElements", this.aerial.elements());
            compound.putDouble("AmplifierDb", this.feed.amplifierDb());
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.tuning = Tuning.readFrom(compound, registries);
        this.links.load(compound);
        if (clientPacket) {
            this.quality = compound.getFloat("Quality");
            this.aerial = AntennaProfile.display(AntennaProfile.Shape.values()[compound.getInt("AntennaShape")], compound.getDouble("AntennaLength"),
                    compound.getInt("AntennaElements"), position());
            this.feed = new FeedLine(0.0, 0.0, 0.0, 0.0, compound.getDouble("AmplifierDb"));
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(AirwaveDataComponents.TUNING, this.tuning);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        Tuning stored = input.get(AirwaveDataComponents.TUNING);
        if (stored != null) {
            this.tuning = stored.clamped();
        }
    }
}
