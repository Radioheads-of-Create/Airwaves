package dev.radioheads.airwaves.content.entity.block;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.RecipeConditions;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import dev.radioheads.airwaves.api.content.item.FoilItem;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.content.recipe.FoilApplicationRecipe;
import dev.radioheads.airwaves.registry.AirwaveBlockEntities;
import dev.radioheads.airwaves.registry.AirwaveRecipeTypes;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

public class FoilApplicatorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, Clearable {
    private static final Object foilRecipesKey = new Object();

    private final SmartInventory foilInventory;
    private FilteringBehaviour filtering;

    private FoilApplicationRecipe currentRecipe;
    private int processingTicks;
    private int emptyTicks;
    private boolean running;

    public LerpedFloat rollSpeed = LerpedFloat.linear();
    private float rollAngle;

    private float syncedSpeed;
    private float lastSentSpeed;

    public FoilApplicatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.foilInventory = new SmartInventory(1, this, (i, item) -> item.getItem() instanceof FoilItem);
    }

    public float getSpeed() {
        if (level != null && level.isClientSide)
            return syncedSpeed;

        return level != null && level.getBlockEntity(worldPosition.below()) instanceof BeltBlockEntity belt
                ? belt.getSpeed()
                : 0;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new FoilApplicatorValueBox()).forRecipes();
        behaviours.add(filtering);

        behaviours.add(new BeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemReceived)
                .whileItemHeld(this::onItemHeld));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;

        if (!level.isClientSide) {
            float speed = getSpeed();
            if (speed != lastSentSpeed) {
                lastSentSpeed = speed;
                sendData();
            }
        }

        manageRotation();

        if (level.isClientSide)
            return;

        if (running && emptyTicks++ >= 2) {
            running = false;
            sendData();
        }

        if (currentRecipe != null)
            processingTicks += Mth.clamp((int) Math.abs(getSpeed() / 64f), 1, 256);
    }

    private void manageRotation() {
        float target = Mth.clamp(getSpeed() * 1.5f, -30, 30);
        rollSpeed.chase(target, 0.2f, LerpedFloat.Chaser.EXP);
        rollSpeed.tickChaser();
        rollAngle = (rollAngle + rollSpeed.getValue()) % 360;
    }

    public float getRollAngle(float partialTicks) {
        return (rollAngle + rollSpeed.getValue(partialTicks) * partialTicks) % 360;
    }

    public ProcessingResult onItemReceived(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (level == null || level.isClientSide || getSpeed() == 0 || getFoil().isEmpty())
            return PASS;

        Optional<FoilApplicationRecipe> recipe = findRecipe(transported.stack);
        if (recipe.isEmpty())
            return PASS;

        currentRecipe = recipe.get();
        processingTicks = 0;
        emptyTicks = 0;
        running = true;
        sendData();
        return HOLD;
    }

    public ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (level == null || currentRecipe == null || getSpeed() == 0 || getFoil().isEmpty()) {
            currentRecipe = null;
            return PASS;
        }

        emptyTicks = 0;
        if (processingTicks < currentRecipe.getProcessingDuration())
            return HOLD;

        List<ItemStack> results = currentRecipe.rollResults(level.random);
        if (results.isEmpty()) {
            currentRecipe = null;
            return PASS;
        }

        List<TransportedItemStack> outputs = new ArrayList<>();
        for (ItemStack result : results) {
            TransportedItemStack output = transported.copy();
            output.stack = result;
            output.locked = true;
            output.angle = BeltHelper.isItemUpright(result) ? 180 : level.random.nextInt(360);
            outputs.add(output);
        }

        int remaining = transported.stack.getCount() - 1;
        foilInventory.extractItem(0, 1, false);
        setChanged();

        if (remaining > 0) {
            TransportedItemStack held = transported.copy();
            held.stack = transported.stack.copyWithCount(remaining);
            handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outputs, held));
        } else {
            handler.handleProcessingOnItem(transported, TransportedResult.convertTo(outputs));
            currentRecipe = null;
        }

        processingTicks = 0;
        sendData();
        return HOLD;
    }

    private Optional<FoilApplicationRecipe> findRecipe(ItemStack input) {
        if (level == null)
            return Optional.empty();

        ItemStack foil = getFoil();
        if (input.isEmpty() || foil.isEmpty())
            return Optional.empty();

        ItemStackHandler handler = new ItemStackHandler(2);
        handler.setStackInSlot(0, input);
        handler.setStackInSlot(1, foil);
        RecipeWrapper wrapper = new RecipeWrapper(handler);

        Predicate<RecipeHolder<? extends Recipe<?>>> types =
                RecipeConditions.isOfType(AirwaveRecipeTypes.FOIL_APPLICATION.getType());

        List<RecipeHolder<? extends Recipe<?>>> candidates = RecipeFinder.get(foilRecipesKey, level, types);

        return candidates.stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe instanceof FoilApplicationRecipe)
                .map(recipe -> (FoilApplicationRecipe) recipe)
                .filter(recipe -> recipe.matches(wrapper, level))
                .filter(recipe -> filtering.test(recipe.getResultItem(level.registryAccess())))
                .findFirst();
    }

    public SmartInventory getFoilInventory() {
        return foilInventory;
    }

    public ItemStack getFoil() {
        return foilInventory.getStackInSlot(0);
    }

    @Nullable
    public FoilType getFoilType() {
        return getFoil().getItem() instanceof FoilItem foil ? foil.getFoilType() : null;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        foilInventory.deserializeNBT(registries, compound.getCompound("FoilInventory"));
        running = compound.getBoolean("Running");
        processingTicks = compound.getInt("ProcessingTicks");

        if (!clientPacket)
            return;

        syncedSpeed = compound.getFloat("Speed");
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("FoilInventory", foilInventory.serializeNBT(registries));
        compound.putBoolean("Running", running);
        compound.putInt("ProcessingTicks", processingTicks);

        if (!clientPacket)
            return;

        compound.putFloat("Speed", getSpeed());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AirwaveBlockEntities.FOIL_APPLICATOR.get(),
                (be, dir) -> be.getFoilInventory());
    }

    @Override
    public void clearContent() {
        foilInventory.clearContent();
        filtering.setFilter(ItemStack.EMPTY);
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, foilInventory);
    }

    static class FoilApplicatorValueBox extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 16.05);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}