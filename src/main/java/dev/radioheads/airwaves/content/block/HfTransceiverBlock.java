package dev.radioheads.airwaves.content.block;

import com.simibubi.create.AllItems;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.content.item.CableItem;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.api.registry.radio.RadioBandEntry;
import dev.radioheads.airwaves.content.entity.block.HfTransceiverBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@NothingNullByDefault
public class HfTransceiverBlock extends CabledKineticBlock<HfTransceiverBlockEntity> {
    public static final BooleanProperty ON_AIR = BooleanProperty.create("on_air");
    public static final EnumProperty<Finish> FINISH = EnumProperty.create("finish", Finish.class);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    public enum Finish implements StringRepresentable {
        BRASS,
        BLACK;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private final int ratedWatts;
    private final RadioBandEntry band;

    public HfTransceiverBlock(Properties properties, int ratedWatts, RadioBandEntry band) {
        super(properties);
        this.ratedWatts = ratedWatts;
        this.band = band;
        registerDefaultState(defaultBlockState().setValue(ON_AIR, false).setValue(FINISH, Finish.BRASS));
    }

    public int ratedWatts() {
        return this.ratedWatts;
    }

    public Tuning defaultTuning() {
        return new Tuning(this.band.get().defaultHertz(), this.band);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ON_AIR, FINISH);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<HfTransceiverBlockEntity> getBlockEntityClass() {
        return HfTransceiverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HfTransceiverBlockEntity> getBlockEntityType() {
        return AirwaveBlockEntities.HF_TRANSCEIVER.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.computeIfAbsent(state.getValue(HORIZONTAL_FACING), HfTransceiverBlock::shape);
    }

    private static VoxelShape shape(Direction facing) {
        VoxelShape shape = box(facing, HfTransceiverControl.BODY);
        for (HfTransceiverControl control : HfTransceiverControl.values()) {
            if (control.kind != HfTransceiverControl.Kind.KEY) {
                shape = Shapes.or(shape, box(facing, control.box));
            }
        }
        return shape.optimize();
    }

    private static VoxelShape box(Direction facing, float[] box) {
        Vec3 a = HfTransceiverPanel.toBlock(facing, box[0], box[1], box[2]);
        Vec3 b = HfTransceiverPanel.toBlock(facing, box[3], box[4], box[5]);
        return Shapes.box(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z), Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }

    @Nullable
    public static HfTransceiverControl controlAt(BlockState state, BlockPos pos, Vec3 hit) {
        return HfTransceiverPanel.controlAt(state.getValue(HORIZONTAL_FACING), pos, hit);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Finish finish = stack.is(Items.BLACK_DYE) ? Finish.BLACK : AllItems.BRASS_SHEET.isIn(stack) ? Finish.BRASS : null;
        if (finish != null && state.getValue(FINISH) != finish) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(FINISH, finish));
                level.playSound(null, pos, finish == Finish.BLACK ? SoundEvents.DYE_USE : SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.8F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof CableItem || AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return press(state, level, pos, player, hit) ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return press(state, level, pos, player, hit) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    private boolean press(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        HfTransceiverControl control = controlAt(state, pos, hit.getLocation());
        if (control == null) {
            return false;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HfTransceiverBlockEntity transceiver) {
            transceiver.press(control, player.isShiftKeyDown());
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.25F, control.kind == HfTransceiverControl.Kind.KEY ? 1.8F : 1.3F);
        }
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof HfTransceiverBlockEntity transceiver) {
            stack.applyComponents(transceiver.collectComponents());
        }
        return withFinish(stack, state.getValue(FINISH));
    }

    public static ItemStack withFinish(ItemStack stack, Finish finish) {
        if (finish != Finish.BRASS) {
            stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(FINISH, finish));
        }
        return stack;
    }
}
