package dev.radioheads.airwaves.api.cable;

import net.minecraft.core.BlockPos;

// Flow seen from this end
public record CableLink(BlockPos other, CableType type, Flow flow) {
    public enum Flow {
        OUT,
        IN,
        // Old saves without flow
        BOTH;

        public Flow reverse() {
            return switch (this) {
                case OUT -> IN;
                case IN -> OUT;
                case BOTH -> BOTH;
            };
        }

        public boolean sends() {
            return this != IN;
        }
    }
}
