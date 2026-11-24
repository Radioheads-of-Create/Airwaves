package dev.radioheads.airwaves.registry;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.radioheads.airwaves.AirwaveLang;
import dev.radioheads.airwaves.Airwaves;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.joml.Vector3f;

import java.util.function.Supplier;

// Some stuff taken from
// https://github.com/The-Metallurgists/Create-TFMG-CE/blob/1.21.1/src/main/java/com/drmangotea/tfmg/registry/TFMGFluids.java
public class AirwaveFluids {
    private AirwaveFluids() {}

    public static final FluidEntry<BaseFlowingFluid.Flowing> SALT_SOLUTION = fluid("salt_solution", 0xDEE4FF);

    public static final FluidEntry<VirtualFluid>
        ETCHING_FLUID = Airwaves.registrate()
            .virtualFluid("etching_fluid")
            .register();

    @SafeVarargs
    private static FluidEntry<BaseFlowingFluid.Flowing> fluid(String name, int fogColor, TagKey<Fluid>... tags) {
        return Airwaves.registrate().fluid(name, getLocation(name), getLocationFlow(name), SolidRenderedPlaceableFluidType.create(fogColor, () -> 1f / 32f))
                .lang(AirwaveLang.toHumanReadable(name))
                .properties(b -> b.viscosity(1000)
                        .density(1000))
                .fluidProperties(p -> p.levelDecreasePerBlock(1)
                        .tickRate(10)
                        .slopeFindDistance(5)
                        .explosionResistance(100f))
                .tag(tags)
                .source(BaseFlowingFluid.Source::new)
                .bucket()
                .tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "buckets/" + name)))
                .build()
                .register();
    }

    public static ResourceLocation getLocation(String name) {
        return Airwaves.asResource("fluid/" + name + "_still");
    }

    public static ResourceLocation getLocationFlow(String name) {
        return Airwaves.asResource("fluid/" + name + "_flow");
    }

    public static void init() {}

    public static class SolidRenderedPlaceableFluidType extends AllFluids.TintedFluidType {
        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        public SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
                                               ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(net.neoforged.neoforge.fluids.FluidStack stack) {
            return NO_TINT;
        }


        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }

        @Override
        protected Vector3f getCustomFogColor() {
            return fogColor;
        }

        @Override
        protected float getFogDistanceModifier() {
            return fogDistance.get();
        }

    }
}
