package dev.radioheads.airwaves.client.render.foil_application;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.api.util.ColorUtil;
import dev.radioheads.airwaves.content.block.FoilApplicatorBlock;
import dev.radioheads.airwaves.content.entity.block.FoilApplicatorBlockEntity;
import dev.radioheads.airwaves.registry.AirwavePartialModels;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class FoilApplicationVisual extends ShaftVisual<FoilApplicatorBlockEntity> {
    protected final RotatingInstance foilRoll;
    @Nullable
    protected RotatingInstance rollBlock;
    private FoilType currentType;

    private final Direction facing;
    private final Vector3f offset;

    public FoilApplicationVisual(VisualizationContext context, FoilApplicatorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        Direction facingDir = blockEntity.getBlockState().getValue(FoilApplicatorBlock.HORIZONTAL_FACING);
        this.facing = facingDir.getCounterClockWise();

        this.offset = switch (facingDir) {
            case DOWN, UP -> new Vector3f();
            case NORTH -> new Vector3f(0, 10/16f + 0.1f, 7/16f);
            case SOUTH -> new Vector3f(0, 10/16f + 0.1f, -7/16f);
            case WEST -> new Vector3f(-7/16f, 10/16f + 0.1f, 0);
            case EAST -> new Vector3f(7/16f, 10/16f + 0.1f, 0);
        };

        foilRoll = instancerProvider()
                .instancer(AllInstanceTypes.ROTATING, Models.partial(AirwavePartialModels.FOIL_ROLL))
                .createInstance();
        setupRotatingInstance(foilRoll);

        FoilType type = blockEntity.getFoilType();

        if (type != null) {
            rollBlock = createBlockInstance(type.color());
            currentType = type;
        }
    }

    private RotatingInstance createBlockInstance(int color) {
        RotatingInstance instance = instancerProvider()
                .instancer(AllInstanceTypes.ROTATING, Models.partial(AirwavePartialModels.FOIL_ROLL_BLOCK))
                .createInstance();

        setupRotatingInstance(instance);
        instance.setColor(ColorUtil.toCatnipColor(color));
        return instance;
    }

    private void setupRotatingInstance(RotatingInstance instance) {
        instance.setPosition(
                        getVisualPosition().getX() + offset.x(),
                        getVisualPosition().getY() + offset.y(),
                        getVisualPosition().getZ() - offset.z())
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
        if (rollBlock != null) {
            relight(rollBlock);
        }
    }

    @Override
    protected void _delete() {
        super._delete();
        foilRoll.delete();
        if (rollBlock != null) {
            rollBlock.delete();
            rollBlock = null;
        }
        currentType = null;
    }

    @Override
    public void update(float partialTick) {
        super.update(partialTick);
        foilRoll.setup(blockEntity, KineticBlockEntityVisual.rotationAxis(blockState), blockEntity.getSpeed() * -1).setChanged();
        if (rollBlock != null) {
            rollBlock.setup(blockEntity, KineticBlockEntityVisual.rotationAxis(blockState), blockEntity.getSpeed() * -1).setChanged();
        }
    }

    @Override
    public void tick(Context context) {
        super.tick(context);

        FoilType foilType = blockEntity.getFoilType();

        if (foilType == null) {
            if (rollBlock != null) {
                rollBlock.delete();
                rollBlock = null;
            }
            currentType = null;
            return;
        }

        if (rollBlock == null || currentType != foilType) {
            rollBlock = createBlockInstance(foilType.color());
            relight(rollBlock);
            currentType = foilType;
        }
    }
}