package dev.radioheads.airwaves.mixins;

import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltProcessingBehaviour.class, remap = false)
public abstract class BeltProcessingBehaviourMixin {
    @Inject(method = "isBlocked", at = @At("HEAD"), cancellable = true)
    private static void airwaves$allowFlushFoilApplicator(BlockGetter world, BlockPos processingSpace, CallbackInfoReturnable<Boolean> cir) {
        if (world.getBlockState(processingSpace.above()).is(AirwaveBlocks.FOIL_APPLICATOR.get()))
            cir.setReturnValue(false);
    }
}
