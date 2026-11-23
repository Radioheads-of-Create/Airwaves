package dev.radioheads.airwaves.registry;

import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.radioheads.airwaves.Airwaves;

public class AirwaveFluids {
    private AirwaveFluids() {}

    public static final FluidEntry<VirtualFluid>
        SALT_SOLUTION = Airwaves.registrate()
            //TODO fix the fluid, why texture no work?? datagen doesnt like .standardFluid
            .virtualFluid("salt_solution")
            .register();

    public static final FluidEntry<VirtualFluid>
        ETCHING_FLUID = Airwaves.registrate()
            .virtualFluid("etching_fluid")
            .register();


    public static void init() {}
}
