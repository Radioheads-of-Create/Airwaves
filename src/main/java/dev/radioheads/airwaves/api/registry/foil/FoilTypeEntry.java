package dev.radioheads.airwaves.api.registry.foil;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.foil.FoilType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FoilTypeEntry extends RegistryEntry<FoilType, FoilType> {
    public FoilTypeEntry(AbstractRegistrate<?> owner, DeferredHolder<FoilType, FoilType> key) {
        super(owner, key);
    }
}
