package dev.radioheads.airwaves.api.cable;

import net.minecraft.core.BlockPos;

public record CableLink(BlockPos other, CableType type) {}
