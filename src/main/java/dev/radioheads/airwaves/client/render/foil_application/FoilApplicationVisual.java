package dev.radioheads.airwaves.client.render.foil_application;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwavePartialModels;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class FoilApplicationVisual extends ShaftVisual<FoilApplicatorBlockEntity> {
    protected final RotatingInstance foilRoll;
    protected final RotatingInstance rollBlock;

    public FoilApplicationVisual(VisualizationContext context, FoilApplicatorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        Direction facing = blockEntity.getBlockState().getValue(FoilApplicatorBlock.HORIZONTAL_FACING).getCounterClockWise();

        Vector3f offset = switch (blockEntity.getBlockState().getValue(FoilApplicatorBlock.HORIZONTAL_FACING)) {
            case DOWN, UP -> new Vector3f();
            case NORTH -> new Vector3f(0, 10/16f + 0.1f, 7/16f);
            case SOUTH -> new Vector3f(0, 10/16f + 0.1f, -7/16f);
            case WEST -> new Vector3f(-7/16f, 10/16f + 0.1f, 0);
            case EAST -> new Vector3f(7/16f, 10/16f + 0.1f, 0);
        };
        foilRoll = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AirwavePartialModels.FOIL_ROLL)).createInstance();
        rollBlock = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AirwavePartialModels.FOIL_ROLL_BLOCK)).createInstance();

        foilRoll.setPosition(getVisualPosition().getX() + offset.x(), getVisualPosition().getY() + offset.y(), getVisualPosition().getZ() - offset.z())
                .rotateToFace(Direction.WEST, facing)
                .setup(blockEntity, KineticBlockEntityVisual.rotationAxis(blockState), blockEntity.getSpeed() * -1);

        rollBlock.setPosition(getVisualPosition().getX() + offset.x(), getVisualPosition().getY() + offset.y(), getVisualPosition().getZ() - offset.z())
                .rotateToFace(Direction.WEST, facing)
                .setup(blockEntity, KineticBlockEntityVisual.rotationAxis(blockState), blockEntity.getSpeed() * -1);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(foilRoll);
        consumer.accept(rollBlock);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(foilRoll);
        relight(rollBlock);
    }

    @Override
    protected void _delete() {
        super._delete();
        foilRoll.delete();
        rollBlock.delete();
    }

    @Override
    public void update(float partialTick) {
        super.update(partialTick);
        foilRoll.setup(blockEntity, KineticBlockEntityVisual.rotationAxis(blockState), blockEntity.getSpeed() * -1).setChanged();
        rollBlock.setup(blockEntity, KineticBlockEntityVisual.rotationAxis(blockState), blockEntity.getSpeed() * -1).setChanged();
    }
}
