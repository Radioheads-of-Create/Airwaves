package dev.radioheads.airwaves.content;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public final class AirwaveArmInteractionPoints {
    public static final Holder.Reference<ArmInteractionPointType> FOIL_APPLICATOR =
            register("foil_applicator", FoilApplicatorPointType::new);

    private static Holder.Reference<ArmInteractionPointType> register(String name, Supplier<? extends ArmInteractionPointType> factory) {
        return Registry.registerForHolder(
                CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE,
                Airwaves.asResource(name),
                factory.get()
        );
    }

    public static void prepare() {}

    public static class FoilApplicatorPointType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return AirwaveBlocks.FOIL_APPLICATOR.has(state);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new FoilApplicatorPoint(this, level, pos, state);
        }
    }

    public static class FoilApplicatorPoint extends ArmInteractionPoint {
        private static final double HOVER_DISTANCE = 0.25;

        public FoilApplicatorPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
            mode = Mode.DEPOSIT;
        }

        @Override
        public void cycleMode() {}

        @Override
        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 1;
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            Direction facing = cachedState.getOptionalValue(FoilApplicatorBlock.HORIZONTAL_FACING).orElse(Direction.SOUTH);

            Vec3 rollCenter = Vec3.atCenterOf(pos).add(FoilApplicatorBlock.rollOffset(facing));
            Vec3 outward = Vec3.atLowerCornerOf(facing.getNormal()).scale(HOVER_DISTANCE);

            return rollCenter.add(outward);
        }

        @Override
        public void updateCachedState() {
            BlockState oldState = cachedState;
            super.updateCachedState();
            if (oldState != cachedState)
                cachedAngles = null;
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            return level.getBlockEntity(pos) instanceof FoilApplicatorBlockEntity applicator
                    && applicator.getFoilInventory().isEmpty()
                    ? applicator.getFoilInventory().insertItem(0, stack, simulate)
                    : stack;
        }
    }
}
