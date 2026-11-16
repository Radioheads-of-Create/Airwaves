package dev.radioheads.airwaves.mixins;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltInventory.class, remap = false)
public abstract class BeltInventoryMixin {
    @Shadow
    @Final
    BeltBlockEntity belt;

    @Inject(method = "getBeltProcessingAtSegment", at = @At("HEAD"), cancellable = true)
    private void airwaves$findFlushFoilApplicator(int segment, CallbackInfoReturnable<BeltProcessingBehaviour> cir) {
        BlockPos beltPos = BeltHelper.getPositionForOffset(belt, segment);
        BlockPos processingPos = beltPos.above();
        if (belt.getLevel().getBlockState(processingPos).is(AirwaveBlocks.FOIL_APPLICATOR.get()))
            cir.setReturnValue(BlockEntityBehaviour.get(belt.getLevel(), processingPos, BeltProcessingBehaviour.TYPE));
    }
}
