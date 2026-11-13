package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.cable.*;
import dev.radioheads.airwaves.client.telephone.TelephoneRing;
import dev.radioheads.airwaves.content.block.HfTransceiverPanel;
import dev.radioheads.airwaves.content.block.TelephoneBlock;
import dev.radioheads.airwaves.content.telephone.EarpieceBinding;
import dev.radioheads.airwaves.content.telephone.PhoneNumbers;
import dev.radioheads.airwaves.content.telephone.TelephoneData;
import dev.radioheads.airwaves.network.telephone.TelephoneDirectory;
import dev.radioheads.airwaves.network.telephone.Telephones;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NothingNullByDefault
public class TelephoneBlockEntity extends SmartBlockEntity implements CableNode {
    public static final double REACH = 6.0;
    public static final int PRESS_TICKS = 4;

    private final CableLinks links = new CableLinks();
    private UUID phone = UUID.randomUUID();
    // Number once on a line
    private String number = "";
    @Nullable
    private UUID earpiece;
    private String dialed = "";
    private boolean ringing;
    private int pressed = -1;
    private long pressedAt;

    public TelephoneBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        if (this.level instanceof ServerLevel server) {
            claim(server);
            Telephones.loaded(this);
            Cables.loaded(server, this.worldPosition, this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (this.level != null && !this.level.isClientSide) {
            Telephones.unloaded(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            TelephoneRing.tick(this);
        }
    }

    // Duplicate numbers dropped
    private void claim(ServerLevel level) {
        if (this.number.isEmpty()) {
            return;
        }
        TelephoneDirectory directory = TelephoneDirectory.of(level);
        TelephoneDirectory.Entry entry = directory.find(this.number);
        Optional<BlockPos> elsewhere = entry == null ? Optional.empty() : entry.pos().filter(pos -> !pos.equals(this.worldPosition));
        boolean copy = entry == null || !entry.phone().equals(this.phone) || elsewhere.filter(pos -> level.isLoaded(pos)
                && level.getBlockEntity(pos) instanceof TelephoneBlockEntity other && other.phone.equals(this.phone)).isPresent();
        if (copy) {
            this.phone = UUID.randomUUID();
            this.number = "";
            this.earpiece = null;
        } else {
            directory.place(this.number, this.phone, this.worldPosition);
        }
        setChanged();
    }

    // Number assignment
    private void number(ServerLevel level) {
        if (!this.number.isEmpty() || this.links.isEmpty()) {
            return;
        }
        CableGraph graph = CableGraph.of(level);
        if (graph.lineAt(this.worldPosition) != LineType.TELEPHONE) {
            return;
        }
        int network = graph.network(this.worldPosition, level.random);
        this.number = TelephoneDirectory.of(level).assign(network, this.phone, this.worldPosition, level.random);
    }

    public void press(int button, ServerPlayer player) {
        this.level.playSound(null, this.worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.15F, 1.6F);
        this.pressed = button;
        this.pressedAt = this.level.getGameTime();
        if (this.earpiece != null && !Telephones.busy(this)) {
            if (button == TelephoneBlock.CALL) {
                Telephones.dial((ServerLevel) this.level, this, player);
            } else if (this.dialed.length() >= PhoneNumbers.LENGTH) {
                Telephones.refuse(player, "length");
            } else {
                this.dialed += button;
            }
        }
        sendData();
    }

    public void erase() {
        if (!this.dialed.isEmpty() && !Telephones.busy(this)) {
            this.dialed = this.dialed.substring(0, this.dialed.length() - 1);
            this.level.playSound(null, this.worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.15F, 1.2F);
            sendData();
        }
    }

    public void clearDial() {
        this.dialed = "";
        sendData();
    }

    // Hook use
    public boolean useEarpiece(Player player, InteractionHand hand, ItemStack stack) {
        EarpieceBinding binding = stack.get(AirwaveDataComponents.EARPIECE);
        boolean pair = binding == null && (this.earpiece == null || (!hooked() && !Telephones.busy(this)));
        boolean hang = binding != null && binding.earpiece().equals(this.earpiece) && !hooked();
        if (!pair && !hang) {
            return false;
        }
        if (this.level.isClientSide) {
            return true;
        }
        if (pair) {
            this.earpiece = UUID.randomUUID();
        }
        stack.shrink(1);
        player.setItemInHand(hand, stack);
        setHooked(true);
        // Hook switch
        this.level.playSound(null, this.worldPosition, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.8F);
        if (hang) {
            Telephones.hungUp(this);
        }
        sendData();
        setChanged();
        return true;
    }

    public void lift(ServerPlayer player) {
        if (!hooked() || this.earpiece == null) {
            return;
        }
        setHooked(false);
        ItemStack stack = earpieceStack();
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, stack);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        this.level.playSound(null, this.worldPosition, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.9F);
        Telephones.answer(this);
        setChanged();
    }

    public ItemStack earpieceStack() {
        ItemStack stack = new ItemStack(AirwaveItems.TELEPHONE_EARPIECE.get());
        if (this.earpiece != null) {
            stack.set(AirwaveDataComponents.EARPIECE, new EarpieceBinding(this.earpiece, this.phone, this.number));
        }
        return stack;
    }

    private void setHooked(boolean hooked) {
        BlockState state = getBlockState().setValue(TelephoneBlock.HOOKED, hooked);
        if (state != getBlockState()) {
            this.level.setBlockAndUpdate(this.worldPosition, state);
        }
    }

    public void ring(boolean ringing) {
        if (this.ringing != ringing) {
            this.ringing = ringing;
            sendData();
        }
    }

    public boolean hooked() {
        return getBlockState().getValue(TelephoneBlock.HOOKED);
    }

    public UUID phone() {
        return this.phone;
    }

    public String number() {
        return this.number;
    }

    @Nullable
    public UUID earpiece() {
        return this.earpiece;
    }

    public String dialed() {
        return this.dialed;
    }

    public boolean ringing() {
        return this.ringing;
    }

    // Button press depth
    public float pressDepth(int button, float partialTicks) {
        if (button != this.pressed || this.level == null) {
            return 0.0F;
        }
        float age = this.level.getGameTime() - this.pressedAt + partialTicks;
        return age < 0.0F || age > PRESS_TICKS ? 0.0F : 1.0F - age / PRESS_TICKS;
    }

    private Direction facing() {
        return getBlockState().getValue(TelephoneBlock.FACING);
    }

    public Vec3 model(Vec3 pixels) {
        return Vec3.atLowerCornerOf(this.worldPosition).add(HfTransceiverPanel.toBlock(facing(), pixels.x, pixels.y, pixels.z));
    }

    @Override
    public CableLinks links() {
        return this.links;
    }

    @Override
    public Vec3 anchor() {
        return model(TelephoneBlock.JACK);
    }

    @Override
    public void cablesChanged() {
        if (this.level instanceof ServerLevel server) {
            number(server);
        }
        sendData();
        setChanged();
    }

    @Override
    public LineType line() {
        return LineType.TELEPHONE;
    }

    // Jack limit
    @Override
    public boolean accepts(CableLink.Flow flow) {
        return this.links.size() < maxLinks() && !this.links.has(flow);
    }

    @Override
    public int maxLinks() {
        return 2;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.links.save(compound);
        compound.putUUID("Phone", this.phone);
        compound.putString("Number", this.number);
        if (this.earpiece != null) {
            compound.putUUID("Earpiece", this.earpiece);
        }
        compound.putString("Dialed", this.dialed);
        if (clientPacket) {
            compound.putBoolean("Ringing", this.ringing);
            compound.putInt("Pressed", this.pressed);
            compound.putLong("PressedAt", this.pressedAt);
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.links.load(compound);
        if (compound.hasUUID("Phone")) {
            this.phone = compound.getUUID("Phone");
        }
        this.number = compound.getString("Number");
        this.earpiece = compound.hasUUID("Earpiece") ? compound.getUUID("Earpiece") : null;
        this.dialed = compound.getString("Dialed");
        if (clientPacket) {
            this.ringing = compound.getBoolean("Ringing");
            this.pressed = compound.getInt("Pressed");
            this.pressedAt = compound.getLong("PressedAt");
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(AirwaveDataComponents.TELEPHONE, new TelephoneData(this.phone, this.number, Optional.ofNullable(this.earpiece)));
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        TelephoneData data = input.get(AirwaveDataComponents.TELEPHONE);
        if (data != null) {
            this.phone = data.phone();
            this.number = data.number();
            this.earpiece = data.earpiece().orElse(null);
        }
    }
}
