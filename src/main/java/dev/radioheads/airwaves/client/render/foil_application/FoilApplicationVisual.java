package dev.radioheads.airwaves.client.render.foil_application;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class FoilApplicationVisual extends KineticBlockEntityVisual<FoilApplicatorBlockEntity> implements SimpleTickableVisual {
    protected final RotatingInstance shaft;

    public FoilApplicationVisual(VisualizationContext context, FoilApplicatorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        Direction facing = blockEntity.getBlockState().getValue(FoilApplicatorBlock.HORIZONTAL_FACING).getCounterClockWise();

        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
                .createInstance()
                .rotateToFace(Direction.SOUTH, facing)
                .setup(blockEntity)
                .setPosition(getVisualPosition());
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(shaft);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(shaft);
    }

    @Override
    protected void _delete() {
        shaft.delete();
    }

    @Override
    public void update(float partialTick) {
        shaft.setup(blockEntity).setChanged();
    }

    @Override
    public void tick(Context context) {
        applyOverstressEffect(blockEntity, shaft);
    }
}
