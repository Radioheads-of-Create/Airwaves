package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface CableNode {
    CableLinks links();

    Vec3 anchor();

    void cablesChanged();

    default int maxLinks() {
        return 4;
    }

    default boolean junction() {
        return false;
    }

    default boolean settled() {
        return true;
    }

    default Optional<AntennaProfile> feeds() {
        return Optional.empty();
    }

    default double amplifierDb() {
        return 0.0;
    }

    // Device line type
    default LineType line() {
        return LineType.RADIO;
    }

    // Wiring line type
    default LineType wiredLine() {
        return line();
    }

    // Line update
    default void lineChanged(LineType line, LineSide side) {}

    // Free jack for this flow
    default boolean accepts(CableLink.Flow flow) {
        return links().size() < maxLinks();
    }
}
