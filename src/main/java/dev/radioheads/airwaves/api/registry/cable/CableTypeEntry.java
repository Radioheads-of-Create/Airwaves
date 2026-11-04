package dev.radioheads.airwaves.api.registry.cable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.cable.CableType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CableTypeEntry extends RegistryEntry<CableType, CableType> {
    public CableTypeEntry(AbstractRegistrate<?> owner, DeferredHolder<CableType, CableType> key) {
        super(owner, key);
    }
}
