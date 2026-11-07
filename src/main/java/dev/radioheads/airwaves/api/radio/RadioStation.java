package dev.radioheads.airwaves.api.radio;

import dev.radioheads.airwaves.api.link.LinkEnd;
import dev.radioheads.airwaves.api.link.RadioTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface RadioStation {
    static UUID blockId(Level level, BlockPos pos) {
        return new UUID(level.dimension().location().hashCode(), pos.asLong());
    }

    UUID id();

    Vec3 antenna();

    default Vec3 position() {
        return antenna();
    }

    default double siteHeight(RadioTerrain terrain) {
        return terrain.siteHeight(antenna());
    }

    long hertz();

    Holder<Modulation> modulation();

    LinkEnd linkEnd(double mhz, double heightBlocks, Vec3 toward);

    default boolean transmits() {
        return false;
    }

    default boolean onAir() {
        return false;
    }

    default boolean receives() {
        return false;
    }

    // Rayleigh fading while moving
    default boolean moving() {
        return false;
    }

    default void hear(float quality) {}

    default float volume() {
        return 1.0F;
    }

    default boolean opensSquelch(float quality) {
        return true;
    }
}
