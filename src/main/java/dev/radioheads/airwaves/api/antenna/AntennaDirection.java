package dev.radioheads.airwaves.api.antenna;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public record AntennaDirection(int degrees) {
    public static final int STEPS = 360;
    public static final AntennaDirection DEFAULT = new AntennaDirection(0);
    public static final Codec<AntennaDirection> CODEC = Codec.INT.xmap(AntennaDirection::of, AntennaDirection::degrees);

    public AntennaDirection {
        if (degrees < 0 || degrees >= STEPS)
            throw new IllegalArgumentException("Direction must be 0-359, got " + degrees);
    }

    public static AntennaDirection of(int raw) {
        return new AntennaDirection(Math.floorMod(raw, STEPS));
    }

    public double dx() { return Math.sin(Math.toRadians(degrees)); }
    public double dz() { return -Math.cos(Math.toRadians(degrees)); }

    public CompassPoint compass() {
        return CompassPoint.nearest(degrees);
    }

    public Component display() {
        return Component.translatable("airwaves.direction.display", degrees, compass().component());
    }
}
