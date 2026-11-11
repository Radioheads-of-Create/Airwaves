package dev.radioheads.airwaves.client.render.transmitter;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.radioheads.airwaves.content.entity.block.TransmitterBlockEntity;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class TransmitterVisual extends KineticBlockEntityVisual<TransmitterBlockEntity> implements SimpleTickableVisual {
    protected final RotatingInstance shaft;

    public TransmitterVisual(VisualizationContext context, TransmitterBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                .createInstance()
                .rotateToFace(Direction.DOWN, Direction.Axis.Z)
                .setup(blockEntity)
                // I'm sure there's a better way to do this, but it works
                .setPosition(new Vector3f(getVisualPosition().getX(), getVisualPosition().getY() - 0.51f, getVisualPosition().getZ()));

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
    public void update(float partialTick) {
        shaft.setup(blockEntity).setChanged();
    }

    @Override
    protected void _delete() {
        shaft.delete();
    }

    @Override
    public void tick(Context context) {
        applyOverstressEffect(blockEntity, shaft);
    }
}
