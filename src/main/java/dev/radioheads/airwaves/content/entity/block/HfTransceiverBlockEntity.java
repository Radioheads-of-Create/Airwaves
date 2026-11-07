package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import dev.radioheads.airwaves.content.block.HfTransceiverBlock;
import dev.radioheads.airwaves.content.block.HfTransceiverControl;
import dev.radioheads.airwaves.content.block.HfTransceiverHints;
import dev.radioheads.airwaves.content.block.HfTransceiverPanel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class HfTransceiverBlockEntity extends TransmitterBlockEntity {
    public static final int SHOW_TICKS = 60;
    public static final int PRESS_TICKS = 4;
    private static final float KNOB_STEP = 0.04F;
    private static final int POWER_STEP_WATTS = 10;

    private boolean standby;
    private boolean locked;
    private boolean megahertz;
    private boolean fast;
    private boolean slotB;
    private long otherHertz = 14_195_000L;
    private final long[] bandMemory = HfTransceiverPanel.defaultBandMemory();
    private float volume = 0.5F;
    private float squelch;
    private int tuningSteps;
    private int pressed = -1;
    private long pressedAt = Long.MIN_VALUE / 2;
    private long antennaShownAt = Long.MIN_VALUE / 2;
    private long volumeShownAt = Long.MIN_VALUE / 2;
    private double feedLossDb;

    public HfTransceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected boolean powered() {
        return !this.standby && super.powered();
    }

    @Override
    public float volume() {
        return this.volume * 2.0F;
    }

    @Override
    public boolean opensSquelch(float quality) {
        return this.squelch <= 0.0F || quality >= this.squelch;
    }

    @Override
    public Vec3 anchor() {
        float[] socket = HfTransceiverControl.ANTENNA;
        return Vec3.atLowerCornerOf(this.worldPosition).add(HfTransceiverPanel.toBlock(facing(), socket[0], socket[1], socket[2]));
    }

    private Direction facing() {
        return getBlockState().getValue(HfTransceiverBlock.HORIZONTAL_FACING);
    }

    public void press(HfTransceiverControl control, boolean sneaking) {
        long now = this.level == null ? 0L : this.level.getGameTime();
        if (control.kind == HfTransceiverControl.Kind.KEY) {
            this.pressed = control.ordinal();
            this.pressedAt = now;
        }
        if (this.standby && control.kind == HfTransceiverControl.Kind.KEY && control != HfTransceiverControl.POWER) {
            changed();
            return;
        }
        switch (control) {
            case POWER -> this.standby = !this.standby;
            case F_LOCK -> this.locked = !this.locked;
            case MHZ -> {
                if (!this.locked) {
                    this.megahertz = !this.megahertz;
                }
            }
            case DOWN -> jumpBand(-1);
            case UP -> jumpBand(1);
            case A_B -> swapSlots();
            case ANT -> this.antennaShownAt = now;
            case PWR_DOWN -> stepPower(-POWER_STEP_WATTS);
            case PWR_UP -> stepPower(POWER_STEP_WATTS);
            case VFO -> this.fast = !this.fast;
            case SQL, AF -> {
                turn(control, sneaking ? -1 : 1);
                return;
            }
        }
        changed();
    }

    public void turn(HfTransceiverControl control, int delta) {
        switch (control) {
            case VFO -> {
                this.tuningSteps += delta;
                if (!this.standby && !this.locked) {
                    long step = this.megahertz ? 1_000_000L : this.fast ? 10_000L : 1_000L;
                    setHertz(tuning().hertz() + delta * step);
                }
            }
            case SQL -> this.squelch = Mth.clamp(this.squelch + delta * KNOB_STEP, 0.0F, 1.0F);
            case AF -> this.volume = Mth.clamp(this.volume + delta * KNOB_STEP, 0.0F, 1.0F);
            default -> {
                return;
            }
        }
        changed();
    }

    private void changed() {
        setChanged();
        sendData();
    }

    private void setHertz(long hertz) {
        retune(tuning().withHertz(hertz));
        int band = HfTransceiverPanel.bandOf(tuning().hertz());
        if (band >= 0) {
            this.bandMemory[band] = tuning().hertz();
        }
    }

    private void jumpBand(int direction) {
        if (!this.locked) {
            setHertz(HfTransceiverPanel.jump(tuning().hertz(), direction, this.megahertz, this.bandMemory));
        }
    }

    private void swapSlots() {
        if (this.locked) {
            return;
        }
        long current = tuning().hertz();
        setHertz(this.otherHertz);
        this.otherHertz = current;
        this.slotB = !this.slotB;
    }

    private void stepPower(int watts) {
        ScrollValueBehaviour power = powerSetting();
        power.setValue(HfTransceiverPanel.powerStep(power.getValue(), watts, powerSteps(), ratedWatts()));
    }

    private int ratedWatts() {
        return ((HfTransceiverBlock) getBlockState().getBlock()).ratedWatts();
    }

    public boolean standby() {
        return this.standby;
    }

    public boolean locked() {
        return this.locked;
    }

    public boolean megahertz() {
        return this.megahertz;
    }

    public boolean fast() {
        return this.fast;
    }

    public boolean slotB() {
        return this.slotB;
    }

    public float knobVolume() {
        return this.volume;
    }

    public float knobSquelch() {
        return this.squelch;
    }

    public int tuningSteps() {
        return this.tuningSteps;
    }

    public float signal() {
        return quality();
    }

    public boolean receiving() {
        return receives() && quality() > 0.0F && opensSquelch(quality());
    }

    public int displayWatts() {
        return (int) Math.round(watts());
    }

    public AntennaProfile antennaProfile() {
        return aerial();
    }

    public double feedLossDb() {
        return this.feedLossDb;
    }

    public HfTransceiverHints.Panel panel() {
        return new HfTransceiverHints.Panel(this.standby, this.locked, this.megahertz, this.fast, this.slotB, hertz(), displayWatts(),
                this.volume, this.squelch, this.tuningSteps, this.pressed, this.pressedAt, antennaLine(), this.feedLossDb);
    }

    public boolean pressedRecently(HfTransceiverControl control, long now) {
        return this.pressed == control.ordinal() && now - this.pressedAt < PRESS_TICKS;
    }

    public boolean powerShown(long now) {
        boolean power = this.pressed == HfTransceiverControl.PWR_DOWN.ordinal() || this.pressed == HfTransceiverControl.PWR_UP.ordinal();
        return power && now - this.pressedAt < SHOW_TICKS / 2;
    }

    public boolean antennaShown(long now) {
        return now - this.antennaShownAt < SHOW_TICKS;
    }

    public boolean volumeShown(long now) {
        return now - this.volumeShownAt < SHOW_TICKS / 2;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("Standby", this.standby);
        compound.putBoolean("Locked", this.locked);
        compound.putBoolean("Megahertz", this.megahertz);
        compound.putBoolean("Fast", this.fast);
        compound.putBoolean("SlotB", this.slotB);
        compound.putLong("OtherHertz", this.otherHertz);
        compound.putLongArray("BandMemory", this.bandMemory);
        compound.putFloat("Volume", this.volume);
        compound.putFloat("Squelch", this.squelch);
        compound.putInt("TuningSteps", this.tuningSteps);
        if (clientPacket) {
            compound.putInt("Pressed", this.pressed);
            compound.putLong("PressedAt", this.pressedAt);
            compound.putLong("AntennaShownAt", this.antennaShownAt);
            compound.putDouble("FeedLossDb", feed().lossDb(hertz() / 1.0e6));
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        float previousVolume = this.volume;
        super.read(compound, registries, clientPacket);
        this.standby = compound.getBoolean("Standby");
        this.locked = compound.getBoolean("Locked");
        this.megahertz = compound.getBoolean("Megahertz");
        this.fast = compound.getBoolean("Fast");
        this.slotB = compound.getBoolean("SlotB");
        if (compound.contains("OtherHertz")) {
            this.otherHertz = compound.getLong("OtherHertz");
        }
        long[] memory = compound.getLongArray("BandMemory");
        if (memory.length == this.bandMemory.length) {
            System.arraycopy(memory, 0, this.bandMemory, 0, memory.length);
        }
        if (compound.contains("Volume")) {
            this.volume = compound.getFloat("Volume");
        }
        this.squelch = compound.getFloat("Squelch");
        this.tuningSteps = compound.getInt("TuningSteps");
        if (clientPacket) {
            this.pressed = compound.getInt("Pressed");
            this.pressedAt = compound.getLong("PressedAt");
            this.antennaShownAt = compound.getLong("AntennaShownAt");
            this.feedLossDb = compound.getDouble("FeedLossDb");
            if (this.level != null && previousVolume != this.volume) {
                this.volumeShownAt = this.level.getGameTime();
            }
        }
    }
}
