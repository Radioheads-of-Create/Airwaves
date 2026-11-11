package dev.radioheads.airwaves.api.foil;

import dev.radioheads.airwaves.api.content.item.FoilItem;

import java.util.function.Supplier;

public record FoilType(Supplier<? extends FoilItem> foilItem, int color) {
}
